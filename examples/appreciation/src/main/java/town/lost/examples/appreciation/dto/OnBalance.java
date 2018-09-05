package town.lost.examples.appreciation.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.wire.LongConversion;
import town.lost.examples.appreciation.util.Balances;

public class OnBalance extends VanillaSignedMessage<OnBalance> {
    @LongConversion(AddressLongConverter.class)
    private long balanceAddress;
    private double amount;
    private double freeAmount;

    public OnBalance() {
    }

    public OnBalance init(long balanceAddress, Balances balances) {
        this.balanceAddress = balanceAddress;
        this.amount = balances.balance();
        this.freeAmount = balances.freeBalance();
        return this;
    }

    public long balanceAddress() {
        return balanceAddress;
    }

    public OnBalance balanceAddress(long balanceAddress) {
        this.balanceAddress = balanceAddress;
        return this;
    }

    public double amount() {
        return amount;
    }

    public OnBalance amount(double amount) {
        this.amount = amount;
        return this;
    }

    public double freeAmount() {
        return freeAmount;
    }

    public OnBalance freeAmount(double freeAmount) {
        this.freeAmount = freeAmount;
        return this;
    }
}
