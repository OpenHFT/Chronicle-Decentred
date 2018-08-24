package town.lost.examples.appreciation.dto;


import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressConverter;
import net.openhft.chronicle.wire.LongConversion;

/**
 * Automated version of Give that is used to allocate new balances for distribution on a regular basis.
 */
public class Topup extends VanillaSignedMessage<Topup> {
    @LongConversion(AddressConverter.class)
    long toAddress;

    double amount;


    public Topup() {
    }

    public Topup(long toAddress, double amount) {
        init(toAddress, amount);
    }

    public Topup init(long toAddress, double amount) {
        this.toAddress = toAddress;
        this.amount = amount;
        return this;
    }

    public long toAddress() {
        return toAddress;
    }

    public Topup toAddress(long toAddress) {
        this.toAddress = toAddress;
        return this;
    }

    public double amount() {
        return amount;
    }

    public Topup amount(double amount) {
        this.amount = amount;
        return this;
    }
}
