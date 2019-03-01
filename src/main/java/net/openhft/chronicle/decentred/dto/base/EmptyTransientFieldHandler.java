package net.openhft.chronicle.decentred.dto.base;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

/**
 * Internal class that provides an implementation of a TransientFieldHandle
 * suitable for messages with no transient fields.
 *
 * @param <T> message type
 */
class EmptyTransientFieldHandler<T extends VanillaSignedMessage<T>> implements TransientFieldHandler<T> {

    private static final TransientFieldHandler<?> INSTANCE = new EmptyTransientFieldHandler<>();

    @SuppressWarnings("unchecked")
    public static <T extends VanillaSignedMessage<T>> TransientFieldHandler<T> instance() {
        return (TransientFieldHandler<T>) INSTANCE;
    }

    private EmptyTransientFieldHandler() {}

    @Override
    public void reset(T original) {}

    @Override
    public void copy(@NotNull T original, @NotNull T target) {}

    @Override
    public void deepCopy(@NotNull T original, @NotNull T target) {}

    @Override
    public void writeMarshallable(@NotNull T original, @NotNull WireOut wire) {}

    @Override
    public void readMarshallable(T original, @NotNull WireIn wire) {}

    @Override
    public void writeMarshallableInternal(T original, BytesOut bytes) {}

    @Override
    public void readMarshallable(@NotNull T original, @NotNull BytesIn bytes) {}
}
