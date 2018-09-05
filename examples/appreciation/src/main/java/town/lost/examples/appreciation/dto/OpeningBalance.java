package town.lost.examples.appreciation.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.wire.LongConversion;

public class OpeningBalance extends VanillaSignedMessage<OpeningBalance> {
    @LongConversion(AddressLongConverter.class)
    private long balanceAddress;

    private double amount;

    public OpeningBalance() {
    }

    public OpeningBalance(long balanceAddress, double amount) {
        init(balanceAddress, amount);
    }

    public OpeningBalance init(long balanceAddress, double amount) {
        this.balanceAddress = balanceAddress;
        this.amount = amount;
        return this;
    }

    public long balanceAddress() {
        return balanceAddress;
    }

    public OpeningBalance balanceAddress(long balanceAddress) {
        this.balanceAddress = balanceAddress;
        return this;
    }

    public double amount() {
        return amount;
    }

    public OpeningBalance amount(double amount) {
        this.amount = amount;
        return this;
    }
}
