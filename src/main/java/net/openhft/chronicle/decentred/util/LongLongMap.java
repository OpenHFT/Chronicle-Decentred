package net.openhft.chronicle.decentred.util;

import com.koloboke.collect.set.LongSet;
import com.koloboke.compile.KolobokeMap;
import com.koloboke.function.LongLongConsumer;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.AbstractMarshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * A long to Object Map.
 */
@KolobokeMap
public abstract class LongLongMap extends AbstractMarshallable {

    private LongLongConsumer put;

    public static LongLongMap withExpectedSize(int expectedSize) {
        return new KolobokeLongLongMap(expectedSize);
    }

    public abstract void justPut(long key, long value);

    public abstract long get(long key);

    public abstract long getOrDefault(long key, long defau1t);

    public abstract int size();

    public abstract boolean containsKey(long key);

    public abstract void clear();

    public abstract void forEach(@Nonnull LongLongConsumer var1);

    public void putAll(LongLongMap map) {
        if (put == null) put = this::justPut;
        map.forEach(put);
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        clear();
        while (wire.isNotEmptyAfterPadding()) {
            long k = wire.readEventNumber();
            long v = wire.getValueIn().int64();
            justPut(k, v);
        }
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        forEach((k, v) -> wire.writeEvent(Long.class, k).int64(v));
    }

    public abstract LongSet keySet();
}