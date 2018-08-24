package town.lost.examples.appreciation.util;

import net.openhft.chronicle.wire.AbstractBytesMarshallable;

public class Balances extends AbstractBytesMarshallable {
    double balance, freeBalance;

    public double balance() {
        return balance;
    }

    public double freeBalance() {
        return freeBalance;
    }

    public double total() {
        return balance + freeBalance;
    }
}
