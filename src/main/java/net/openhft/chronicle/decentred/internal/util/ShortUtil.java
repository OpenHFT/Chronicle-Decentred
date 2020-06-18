package net.openhft.chronicle.decentred.internal.util;

public enum ShortUtil {;

    private static int MAX_UNSIGNED_VALUE = (1 << Short.SIZE) - 1;

    /**
     * Checks that the specified int can be represented by an unsigned short. This
     * method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = ShortUtil.requireUnsignedShort(bar);
     * }
     * </pre></blockquote>
     *
     * @param value the int value to check range for
     * @return the int value if in range
     * @throws ArithmeticException if {@code int} is out of range
     */
    public static int requireUnsignedShort(int value) {
        if (value < 0 || value > MAX_UNSIGNED_VALUE) {
            throw new ArithmeticException("unsigned short overflow");
        }
        return value;
    }

    /**
     * Checks that the specified int can be represented by a positive unsigned short. This
     * method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int bar) {
     *     this.bar = ShortUtil.requirePositiveUnsignedShort(bar);
     * }
     * </pre></blockquote>
     *
     * @param value the int value to check range for
     * @return the int value if in range
     * @throws ArithmeticException if {@code int} is out of range or
     * not positive
     */
    public static int requirePositiveUnsignedShort(int value) {
        if (value <= 0) {
            throw new ArithmeticException("value is not positive");
        }
        if (value > MAX_UNSIGNED_VALUE) {
            throw new ArithmeticException("unsigned short overflow");
        }
        return value;
    }

/**
     * Returns the value of the {@code int} argument;
     * throwing an exception if the value overflows a {@code short}.
     *
     * @param value the int value
     * @return the argument as a short
     * @throws ArithmeticException if the {@code argument} overflows a short
     */
    public static short toShortExact(int value) {
        if ((short) value != value) {
            throw new ArithmeticException("short overflow");
        }
        return (short) value;
    }

}
