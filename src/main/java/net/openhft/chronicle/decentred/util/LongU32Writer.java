package net.openhft.chronicle.decentred.util;

import com.koloboke.function.LongLongConsumer;
import net.openhft.chronicle.bytes.BytesOut;

import static java.util.Objects.requireNonNull;

/**
 * Class used for writing a long and and an unsigned int to a ByteOut.
 *
 * This class is mutable and not thread safe.
 */
public final class LongU32Writer implements LongLongConsumer {

    private BytesOut<?> bytes;

    /**
     * Writes the provided parameters into a previously set
     * {@link #bytes}.
     *
     * @param k long to write
     * @param v unsigned int to write
     * @throws NullPointerException if the method {@link #bytes(BytesOut)}
     * was not invoked before this method.
     */
    @Override
    public void accept(long k, long v) {
        bytes.writeLong(k).writeUnsignedInt(v);
    }

    /**
     * Sets the ByteOut to use for writing when subsequent calls
     * to the {@link #accept(long, long)} method are made.
     * <p>
     * This method must be invoked before the {@link #accept(long, long)} method.
     *
     * @param bytes to use for writing
     * @throws NullPointerException if the provided {@code bytes}
     * is {@code null}
     *
     */
    public void bytes(BytesOut<?> bytes) {
        this.bytes = requireNonNull(bytes);
    }

}
