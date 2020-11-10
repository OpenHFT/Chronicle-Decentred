package net.openhft.chronicle.decentred.util;

import com.koloboke.function.LongLongConsumer;
import net.openhft.chronicle.bytes.BytesOut;

/**
 * Class used for writing a long and and an unsigned int to a ByteOut.
 *
 */
public final class LongU32Writer implements LongLongConsumer {

    private final BytesOut<?> bytes;

    public LongU32Writer(BytesOut<?> bytes) {
        this.bytes = bytes;
    }

    /**
     * Writes the provided parameters into an internal {@link #bytes}.
     *
     * @param k long to write
     * @param v unsigned int to write
     */
    @Override
    public void accept(long k, long v) {
        bytes.writeLong(k).writeUnsignedInt(v);
    }

/*    *//**
     * Sets the ByteOut to use for writing when subsequent calls
     * to the {@link #accept(long, long)} method are made.
     * <p>
     * This method must be invoked before the {@link #accept(long, long)} method.
     *
     * @param bytes to use for writing
     * @throws NullPointerException if the provided {@code bytes}
     * is {@code null}
     *
     *//*
    public void bytes(BytesOut<?> bytes) {
        this.bytes = requireNonNull(bytes);
    }*/

}
