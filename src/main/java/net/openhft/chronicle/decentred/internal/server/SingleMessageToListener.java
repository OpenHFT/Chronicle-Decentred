package net.openhft.chronicle.decentred.internal.server;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.server.RunningMessageToListener;
import net.openhft.chronicle.threads.LongPauser;
import net.openhft.chronicle.threads.Pauser;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class SingleMessageToListener implements RunningMessageToListener, Runnable {

    private final Pauser pauser = new LongPauser(0, 10, 1, 20, TimeUnit.MILLISECONDS);
    private final MessageToListener server;
    private final AtomicReference<Bytes> writeLock = new AtomicReference<>();
    private final VanillaSignedMessage signedMessage = new VanillaSignedMessage(){};
    private final Bytes bytes1 = Bytes.allocateElasticDirect(32 << 20).unchecked(true);
    private final Bytes bytes2 = Bytes.allocateElasticDirect(32 << 20).unchecked(true);

    public SingleMessageToListener(@NotNull MessageToListener server) {
        this.server = server;
        writeLock.set(bytes1);
    }

    @Override
    public List<Runnable> runnables() {
        return Collections.singletonList(this);
    }

    @Override
    public void onMessageTo(long address, SignedMessage message) {
        Bytes bytes = lock();
        try {
            long position = bytes.writePosition();
            bytes.ensureCapacity(position + (1 << 16));
            bytes.writeInt(0);
            bytes.writeLong(address);
            message.writeMarshallable(bytes);
            bytes.writeInt(position, (int) (bytes.writePosition() - position - 4));
        } finally {
            unlock(bytes);
        }
        pauser.unpause();
    }

    private Bytes lock() {
        return writeLock.getAndSet(null);
    }

    private void unlock(Bytes bytes) {
        writeLock.set(bytes);
    }

    boolean flush() {
        Bytes bytes = writeLock.get();
        if (bytes == null)
            return false;
        Bytes other = bytes == bytes1 ? bytes2 : bytes1;
        if (!writeLock.compareAndSet(bytes, other))
            return false;

        if (bytes.writePosition() == 0)
            return false;

        long limit = bytes.readLimit();
        while (bytes.readRemaining() > 0) {
            int size = bytes.readInt();
            long end = bytes.readPosition() + size;
            bytes.readLimit(end);
            try {
                long address = bytes.readLong();
                signedMessage.readMarshallable(bytes);
                server.onMessageTo(address, signedMessage);
            } finally {
                bytes.readPosition(end);
                bytes.readLimit(limit);
            }
        }
        bytes.clear();
        return true;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (flush())
                    pauser.reset();
                else
                    pauser.pause();
            }
        } catch (Throwable t) {
            Jvm.warn().on(getClass(), "Writer died", t);
        }
    }
}

