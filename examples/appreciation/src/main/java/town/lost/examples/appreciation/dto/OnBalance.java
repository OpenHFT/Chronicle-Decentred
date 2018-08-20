package town.lost.examples.appreciation.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressConverter;
import net.openhft.chronicle.wire.LongConversion;

public class OnBalance extends VanillaSignedMessage<OnBalance> {
    @LongConversion(AddressConverter.class)
    private long balanceAddress;
    private double amount;

    public OnBalance() {
    }

    public OnBalance(long balanceAddress, double amount) {
        init(balanceAddress, amount);
    }

    public OnBalance init(long balanceAddress, double amount) {
        this.balanceAddress = balanceAddress;
        this.amount = amount;
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
}
