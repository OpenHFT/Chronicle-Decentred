package net.openhft.chronicle.decentred.internal.unmodifiable;

import com.koloboke.collect.set.LongSet;
import com.koloboke.function.LongLongConsumer;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import static net.openhft.chronicle.decentred.internal.unmodifiable.ThrowUtil.newUnsupportedOperationException;

public final class UnmodifiableLongLongMap extends net.openhft.chronicle.decentred.util.LongLongMap {

    private final net.openhft.chronicle.decentred.util.LongLongMap inner;

    public UnmodifiableLongLongMap(@NotNull net.openhft.chronicle.decentred.util.LongLongMap inner) {
        this.inner = inner;
    }

    @Override public void justPut(long key, long value) { throw newUnsupportedOperationException(); }
    @Override public long get(long key) { return inner.get(key); }
    @Override public long getOrDefault(long key, long defau1t) { return inner.getOrDefault(key, defau1t); }
    @Override public int size() { return inner.size(); }
    @Override public boolean containsKey(long key) { return inner.containsKey(key); }
    @Override public void clear() { throw newUnsupportedOperationException(); }
    @Override public void forEach(@Nonnull LongLongConsumer var1) { inner.forEach(var1); }
    @Override public void putAll(net.openhft.chronicle.decentred.util.LongLongMap map) { throw newUnsupportedOperationException(); }
    @Override public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException { throw newUnsupportedOperationException(); }
    @Override public void writeMarshallable(@NotNull WireOut wire) { inner.writeMarshallable(wire); }
    @Override @Nonnull public LongSet keySet() { return new UnmodifiableLongSet(inner.keySet()); }
    @Override public boolean equals(Object o) { return inner.equals(o); }
    @Override public int hashCode() { return inner.hashCode(); }
    @Override public String toString() { return inner.toString(); }
    @Override @Nullable public <T> T getField(String name, Class<T> tClass) throws NoSuchFieldException { return inner.getField(name, tClass); }
    @Override public void setField(String name, Object value) throws NoSuchFieldException { throw newUnsupportedOperationException(); }
    @Override @NotNull public <T> T deepCopy() { return inner.deepCopy(); }
    @Override @NotNull @Deprecated public <T extends Marshallable> T copyFrom(@NotNull T t) { return inner.copyFrom(t); }
    @Override public <T extends Marshallable> T copyTo(@NotNull T t) { return inner.copyTo(t); }
    @Override public <K, T extends Marshallable> T mergeToMap(@NotNull Map<K, T> map, @NotNull Function<T, K> getKey) { return inner.mergeToMap(map, getKey); }
    @Override @NotNull public List<FieldInfo> $fieldInfos() { return inner.$fieldInfos(); }
    @Override public String getClassName() { return inner.getClassName(); }
    @Override public void reset() { throw newUnsupportedOperationException(); }
    @Override public void writeValue(@NotNull ValueOut out) { inner.writeValue(out); }
    @Override public void unexpectedField(Object event, ValueIn valueIn) { inner.unexpectedField(event, valueIn); }
    @Override public void readMarshallable(BytesIn bytes) throws IORuntimeException { throw newUnsupportedOperationException(); }
    @Override public void writeMarshallable(BytesOut bytes) { inner.writeMarshallable(bytes); }
    @Override public String $toString() { return inner.$toString();}

}
