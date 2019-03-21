package net.openhft.chronicle.decentred.internal.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

final class ShortUtilTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, Short.MAX_VALUE, 65535})
    void requireUnsignedShort(int i) {
        assertEquals(i, ShortUtil.requireUnsignedShort(i));
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE})
    void requireUnsignedShortThrows(int i) {
        assertThrows(ArithmeticException.class, () -> {
            ShortUtil.requireUnsignedShort(i);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, Short.MAX_VALUE, 65535})
    void requirePositiveUnsignedShort(int i) {
        assertEquals(i, ShortUtil.requirePositiveUnsignedShort(i));
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 65536, Integer.MAX_VALUE})
    void requirePositiveUnsignedShortThrows(int i) {
        assertThrows(ArithmeticException.class, () -> {
            ShortUtil.requirePositiveUnsignedShort(i);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {Short.MIN_VALUE, -1, 0, 1, 2, Short.MAX_VALUE})
    void toShortExact(int i) {
        assertEquals(i, ShortUtil.toShortExact(i));
    }

    @ParameterizedTest
    @ValueSource(ints = {Short.MIN_VALUE - 1, Short.MAX_VALUE + 1})
    void toShortExactThrows(int i) {
        assertThrows(ArithmeticException.class, () -> {
            ShortUtil.toShortExact(i);
        });

    }
}