package town.lost.examples.exchange.util;

import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;

public enum Validators {
    ; // none

    public static double notNaN(double value) {
        return notNaN(value, "");
    }

    public static double notNaN(double value, String message) {
        if (isNaN(value))
            throw new IllegalArgumentException(message);

        return value;
    }

    public static double notInfinite(double value) {
        if (isInfinite(value))
            throw new IllegalArgumentException();
        return value;
    }

    public static double validNumber(double value) {
        return notInfinite(notNaN(value));
    }

    public static double positive(double value) {
        if (value >= 0)
            return value;
        throw new IllegalArgumentException();
    }

    public static long positive(long value) {
        if (value >= 0)
            return value;
        throw new IllegalArgumentException();
    }
}
