package town.lost.examples.appreciation;

import net.openhft.chronicle.decentred.util.LongDoubleMap;

public class VanillaBalanceStore implements BalanceStore {
    private final LongDoubleMap amountsMap = LongDoubleMap.withExpectedSize(1024);

    @Override
    public double getBalance(long address) {
        double amount;
        synchronized (amountsMap) {
            amount = amountsMap.getOrDefault(address, Long.MIN_VALUE);
        }
        return amount == Long.MIN_VALUE ? Double.NaN : amount;
    }

    @Override
    public boolean subtractBalance(long address, double amount) {
        assert amount >= 0;
        synchronized (amountsMap) {
            double amount2 = amountsMap.getOrDefault(address, 0);
            amount2 -= amount;
            if (amount2 < 0)
                return false;
            amountsMap.justPut(address, amount2);
            return true;
        }
    }

    @Override
    public void addBalance(long address, double amount) {
        assert amount >= 0;
        synchronized (amountsMap) {
            double amount2 = amountsMap.getOrDefault(address, 0);
            amount2 += amount;
            amountsMap.justPut(address, amount2);
        }
    }

    @Override
    public void setBalance(long address, double amount) {
        assert amount >= 0;
        synchronized (amountsMap) {
            amountsMap.justPut(address, amount);
        }
    }
}
