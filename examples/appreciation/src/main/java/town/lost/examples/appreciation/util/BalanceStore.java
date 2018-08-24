package town.lost.examples.appreciation.util;

public interface BalanceStore {
    Balances getBalances(long address);

    boolean subtractBalance(long address, double amount);

    void addBalance(long address, double amount);

    void setBalance(long address, double amount);

    void setFreeBalance(double amount);
}
