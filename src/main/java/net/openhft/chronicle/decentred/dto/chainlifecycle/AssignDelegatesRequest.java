package net.openhft.chronicle.decentred.dto.chainlifecycle;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.NativeBytesStore;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.dto.base.TransientFieldHandler;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.dto.error.ApplicationErrorResponse;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

// Support for this will be added later.
public final class AssignDelegatesRequest extends VanillaSignedMessage<AssignDelegatesRequest> {

    private transient List<BytesStore> delegates = new ArrayList<>();

    public List<BytesStore> delegates() {
        return Collections.unmodifiableList(delegates);
    }

    public AssignDelegatesRequest delegates(List<BytesStore> delegates) {
        assertNotSigned();
        this.delegates = new ArrayList<>(requireNonNull(delegates));
        return this;
    }

// Handling of transient fields

    private static final TransientFieldHandler<AssignDelegatesRequest> TRANSIENT_FIELD_HANDLER = new CustomTransientFieldHandler();

    @Override
    public TransientFieldHandler<AssignDelegatesRequest> transientFieldHandler() {
        return TRANSIENT_FIELD_HANDLER;
    }

    private static final class CustomTransientFieldHandler implements TransientFieldHandler<AssignDelegatesRequest> {

        @Override
        public void reset(AssignDelegatesRequest original) {
            original.delegates.clear();
        }

        @Override
        public void copyNonMarshalled(@NotNull AssignDelegatesRequest original, @NotNull AssignDelegatesRequest target) {
            // All transient fields are marshalled
        }

        @Override
        public void writeMarshallable(@NotNull AssignDelegatesRequest original, @NotNull WireOut wire) {
            wire.write("delegates").sequence(original.delegates, (d, v) -> {
                for (BytesStore bytesStore : d) {
                    v.bytes(bytesStore);
                }
            });
        }

        @Override
        public void readMarshallable(AssignDelegatesRequest original, WireIn wire) {
            wire.read("delegates").sequence(original.delegates, (d, v) -> {
                while (v.hasNextSequenceItem()) {
                    byte[] bytes = v.bytes();
                    d.add(NativeBytesStore.from(bytes));
                }
            });
        }

@Override
        public void writeMarshallableInternal(AssignDelegatesRequest original, BytesOut bytes) {
            bytes.writeStopBit(original.delegates.size());
            for (BytesStore delegate : original.delegates) {
                bytes.write(delegate);
            }
        }

        @Override
        public void readMarshallable(@NotNull AssignDelegatesRequest original, @NotNull BytesIn bytes) {
            final int length = (int) bytes.readStopBit();
            original.delegates.clear();
            for (int i = 0; i < length; i++) {
                final int pklen = Ed25519.PUBLIC_KEY_LENGTH;
                final BytesStore bs = NativeBytesStore.nativeStoreWithFixedCapacity(pklen);
                bytes.copyTo(bs);
                original.delegates.add(bs);
            }
        }
    }

}
