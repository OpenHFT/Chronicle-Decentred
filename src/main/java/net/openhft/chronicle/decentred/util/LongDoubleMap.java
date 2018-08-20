package net.openhft.chronicle.decentred.util;

import com.koloboke.compile.KolobokeMap;
import net.openhft.chronicle.wire.AbstractMarshallable;

@KolobokeMap
public abstract class LongDoubleMap extends AbstractMarshallable {
    public static LongDoubleMap withExpectedSize(int expectedSize) {
        return new KolobokeLongDoubleMap(expectedSize);
    }

    public abstract void justPut(long key, double value);

    public abstract double getOrDefault(long key, double defau1t);

    public abstract String toString();

}
