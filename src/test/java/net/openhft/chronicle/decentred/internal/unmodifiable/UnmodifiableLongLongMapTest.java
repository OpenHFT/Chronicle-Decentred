package net.openhft.chronicle.decentred.internal.unmodifiable;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.decentred.util.LongLongMap;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class UnmodifiableLongLongMapTest {

    private LongLongMap m;
    private LongLongMap inner;

    @BeforeEach
    void init() {
        inner = LongLongMap.withExpectedSize(10);
        inner.justPut(1, 1);
        inner.justPut(2, 2);
        m = new UnmodifiableLongLongMap(inner);
    }

    @Test
    void justPut() {
        assertThrowsUnsupportedOperationException(() -> {
            m.justPut(0, 0);
        });
    }

    @Test
    void get() {
        assertEquals(1, m.get(1));
    }

    @Test
    void getOrDefault() {
        assertEquals(1, m.getOrDefault( 100,1));
    }

    @Test
    void size() {
        assertEquals(2, m.size());
    }

    @Test
    void containsKey() {
        assertTrue(m.containsKey(1));
    }

    @Test
    void clear() {
        assertThrowsUnsupportedOperationException(m::clear);
    }

    @Test
    void forEach() {
        final Map<Long, Long> expected = new HashMap<Long, Long>(){{
            put(1L, 1L);
            put(2L, 2L);
        }};
        final Map<Long, Long> actual = new HashMap<>();
        m.forEach(actual::put);
        assertEquals(expected, actual);
    }

    @Test
    void putAll() {
        final LongLongMap otherMap = LongLongMap.withExpectedSize(10);
        otherMap.justPut(5, 5);
        assertThrowsUnsupportedOperationException(() -> m.putAll(otherMap));
    }

    @Test
    void readMarshallable() {
        final Bytes bytes = Bytes.elasticByteBuffer(1000);
        final WireIn wireIn = new TextWire(bytes);
        assertThrowsUnsupportedOperationException(() -> m.readMarshallable(wireIn));
    }

    @Test
    void writeMarshallable() {
        final Bytes bytes = Bytes.elasticByteBuffer(1000);
        final WireOut wireOut = new TextWire(bytes);
        m.writeMarshallable(wireOut);
    }

    @Test
    void keySet() {
        // Todo
    }

    @Test
    void testEquals() {
        assertTrue(m.equals(inner));

        // Huston, we have had a problem here..
        // assertEquals(inner, m);
    }

    @Test
    void testHashCode() {
        assertEquals(inner.hashCode(), m.hashCode());
    }

    @Test
    void testToString() {
        assertEquals(inner.toString(), m.toString());
    }

    @Test
    void getField() {
        //Todo
    }

    @Test
    void setField() {
        assertThrowsUnsupportedOperationException(() -> {
            try {
                m.setField("foo","123");
            } catch (NoSuchFieldException e) {
            }
        });
    }

    @Test
    void deepCopy() {
    }

    @Test
    void copyFrom() {
    }

    @Test
    void copyTo() {
    }

    @Test
    void mergeToMap() {
    }

    @Test
    void reset() {
        assertThrowsUnsupportedOperationException(() -> {
            m.reset();
        });
    }

    private void assertThrowsUnsupportedOperationException(Runnable e) {
        assertThrows(UnsupportedOperationException.class, e::run);
    }
}