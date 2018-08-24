package town.lost.examples.exchange.api;

import net.openhft.chronicle.core.annotation.NotNull;
import town.lost.examples.exchange.util.CouldBeNaN;

import static java.lang.Math.nextUp;

public enum Side {
    BUY {
        @Override
        public @NotNull
        int compare(double newPrice, double referencePrice, double precision) {
            if (newPrice > referencePrice + precision)
                return +1;
            if (referencePrice > newPrice + precision)
                return -1;
            return 0;
        }

        @Override
        public @CouldBeNaN
        double roundWorse(@CouldBeNaN double value, @CouldBeNaN double tickSize) {
            return tickSize * Math.floor(value / tickSize);
        }

        @Override
        public Side other() {
            return SELL;
        }

    },
    SELL {
        @Override
        public int compare(double newPrice, double referencePrice, double precision) {
            return BUY.compare(referencePrice, newPrice, precision);
        }

        @Override
        public @CouldBeNaN
        double roundWorse(@CouldBeNaN double value, @CouldBeNaN double tickSize) {
            return tickSize * Math.ceil(value / tickSize);
        }

        @Override
        public Side other() {
            return BUY;
        }

    };

    static final double DEFAULT_PRECISION_FACTOR = 1E-7;

    static {
        assert (BUY.ordinal() == 0) && (SELL.ordinal() == 1);
    }

    public static double getDefaultPrecision(double tickSize) {
        return nextUp(tickSize * DEFAULT_PRECISION_FACTOR);
    }

    @NotNull
    protected abstract int compare(double newPrice, double referencePrice, double precision);

    public boolean isBetterOrSame(double price, double referencePrice, double precision) {
        return compare(price, referencePrice, precision) >= 0;
    }

    public abstract double roundWorse(double value, double precision);

    public abstract Side other();
}
