package net.openhft.chronicle.decentred.util;

import com.koloboke.compile.KolobokeMap;
import com.koloboke.function.LongObjConsumer;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.AbstractMarshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongFunction;

/**
 * A long to Object Map.
 */
@KolobokeMap
public abstract class LongObjMap<V> extends AbstractMarshallable {
    Class<V> vClass;

    public static <V> LongObjMap<V> withExpectedSize(Class<V> vClass, int expectedSize) {
        KolobokeLongObjMap<V> kolobokeXCLLongObjMap = new KolobokeLongObjMap<>(expectedSize);
        kolobokeXCLLongObjMap.vClass = vClass;
        return kolobokeXCLLongObjMap;
    }

    public abstract void justPut(long key, V value);

    public abstract V get(long key);

    public abstract int size();

    public abstract boolean containsKey(long key);

    public abstract void clear();

    public abstract void forEach(LongObjConsumer<? super V> longObjConsumer);

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        clear();
        while (wire.isNotEmptyAfterPadding()) {
            long k = wire.readEventNumber();
            V v = wire.getValueIn().object(vClass);
            justPut(k, v);
        }
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        forEach((k, v) -> wire.writeEvent(Long.class, k).object(vClass, v));
    }

    public abstract V computeIfAbsent(long key, LongFunction<? extends V> supplier);

    public abstract boolean justRemove(long key);
}