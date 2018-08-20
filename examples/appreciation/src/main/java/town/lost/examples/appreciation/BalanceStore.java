package town.lost.examples.appreciation;

public interface BalanceStore {
    double getBalance(long address);

    boolean subtractBalance(long address, double amount);

    void addBalance(long address, double amount);

    void setBalance(long address, double amount);
}
