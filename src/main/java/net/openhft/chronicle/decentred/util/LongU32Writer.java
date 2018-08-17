package net.openhft.chronicle.decentred.util;

import com.koloboke.function.LongLongConsumer;
import net.openhft.chronicle.bytes.BytesOut;

public class LongU32Writer implements LongLongConsumer {
    public BytesOut<?> bytes;

    @Override
    public void accept(long k, long v) {
        bytes.writeLong(k).writeUnsignedInt(v);
    }
}
