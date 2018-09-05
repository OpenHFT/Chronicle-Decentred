package town.lost.examples.appreciation.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.wire.LongConversion;

public class Give extends VanillaSignedMessage<Give> {
    @LongConversion(AddressLongConverter.class)
    private long toAddress;

    private double amount;

    public Give() {
    }

    public Give(long toAddress, double amount) {
        init(toAddress, amount);
    }

    public Give init(long toAddress, double amount) {
        this.toAddress = toAddress;
        this.amount = amount;
        return this;
    }

    public long toAddress() {
        return toAddress;
    }

    public Give toAddress(long toAddress) {
        this.toAddress = toAddress;
        return this;
    }

    public double amount() {
        return amount;
    }

    public Give amount(double amount) {
        this.amount = amount;
        return this;
    }
}
