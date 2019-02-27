package net.openhft.chronicle.decentred.dto.chainlifecycle;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.NativeBytesStore;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

// Support for this will be added later.
public class AssignDelegatesRequest extends VanillaSignedMessage<AssignDelegatesRequest> {

    private transient List<BytesStore> delegates = new ArrayList<>();

    public List<BytesStore> delegates() {
        return Collections.unmodifiableList(delegates);
    }

    public AssignDelegatesRequest delegates(List<BytesStore> delegates) {
        assert !signed();
        this.delegates = new ArrayList<>(requireNonNull(delegates));
        return this;
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        super.readMarshallable(wire);
        wire.read("delegates").sequence(delegates, (d, v) -> {
            while (v.hasNextSequenceItem()) {
                byte[] bytes = v.bytes();
                d.add(NativeBytesStore.from(bytes));
            }
        });
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        super.writeMarshallable(wire);
        wire.write("delegates").sequence(delegates, (d, v) -> {
            for (BytesStore bytesStore : d) {
                v.bytes(bytesStore);
            }
        });
    }

    @Override
    public void readMarshallable(BytesIn bytes) throws IORuntimeException {
        super.readMarshallable(bytes);
        int length = (int) bytes.readStopBit();
        delegates.clear();
        for (int i = 0; i < length; i++) {
            int pklen = Ed25519.PUBLIC_KEY_LENGTH;
            BytesStore bs = NativeBytesStore.nativeStoreWithFixedCapacity(pklen);
            bytes.copyTo(bs);
            delegates.add(bs);
        }
    }

    @Override
    protected void writeMarshallable0(BytesOut bytes) {
        super.writeMarshallable0(bytes);
        bytes.writeStopBit(delegates.size());
        for (BytesStore delegate : delegates) {
            bytes.write(delegate);
        }
    }
}
