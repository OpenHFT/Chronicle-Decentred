package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.dto.SignedMessage;
import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.threads.LongPauser;
import net.openhft.chronicle.threads.Pauser;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SingleMessageListener implements RunningMessageListener, Runnable {
    private final Pauser pauser = new LongPauser(0, 10, 1, 20, TimeUnit.MILLISECONDS);
    private final MessageListener xclServer;
    private final AtomicReference<Bytes> writeLock = new AtomicReference<>();
    private final VanillaSignedMessage signedMessage = new VanillaSignedMessage();
    private final Bytes bytes1 = Bytes.allocateElasticDirect(32 << 20).unchecked(true);
    private final Bytes bytes2 = Bytes.allocateElasticDirect(32 << 20).unchecked(true);

    public SingleMessageListener(MessageListener xclServer) {
        this.xclServer = xclServer;
        writeLock.set(bytes1);
    }

    @Override
    public Runnable[] runnables() {
        return new Runnable[]{this};
    }

    @Override
    public void onMessage(SignedMessage message) {
        onMessageTo(0L, message);
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
                xclServer.onMessageTo(address, signedMessage);
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

