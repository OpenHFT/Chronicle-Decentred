package town.lost.examples.appreciation.util;

import net.openhft.chronicle.decentred.util.LongObjMap;

public class VanillaBalanceStore implements BalanceStore {
    private final LongObjMap<Balances> amountsMap = LongObjMap.withExpectedSize(Balances.class, 1024);

    @Override
    public Balances getBalances(long address) {
        synchronized (amountsMap) {
            return amountsMap.get(address);
        }
    }

    @Override
    public boolean subtractBalance(long address, double amount) {
        assert amount >= 0;
        synchronized (amountsMap) {
            Balances balances = amountsMap.get(address);
            if (balances == null)
                return false;
            if (balances.total() < amount)
                return false;
            double free2 = Math.min(balances.freeBalance, amount);
            balances.freeBalance -= free2;
            amount -= free2;

            double balance = Math.min(balances.balance, amount);
            balances.balance -= balance;
            return true;
        }
    }

    @Override
    public void addBalance(long address, double amount) {
        assert amount >= 0;
        synchronized (amountsMap) {
            Balances balances = amountsMap.get(address);
            if (balances == null) {
                amountsMap.justPut(address, balances);
            }
            balances.balance += amount;
        }
    }

    @Override
    public void setBalance(long address, double amount) {
        assert amount >= 0;
        synchronized (amountsMap) {
            Balances balances = amountsMap.get(address);
            if (balances == null) {
                amountsMap.justPut(address, balances = new Balances());
            }
            balances.balance = amount;
        }
    }

    @Override
    public void setFreeBalance(double amount) {
        synchronized (amountsMap) {
            amountsMap.forEach((k, b) -> b.freeBalance = amount);
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        amountsMap.forEach((address, balance)  -> {
           builder.append("" + address + " has " + balance.balance());
        });
        return builder.toString();
    }
}
