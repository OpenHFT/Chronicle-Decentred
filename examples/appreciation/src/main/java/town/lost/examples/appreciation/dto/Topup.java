package town.lost.examples.appreciation.dto;


import net.openhft.chronicle.core.annotation.UsedViaReflection;
import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressConverter;
import net.openhft.chronicle.wire.LongConversion;

/**
 * Automated version of Give that is used to allocate new balances for distribution on a regular basis.
 */
public class Topup extends VanillaSignedMessage<Topup> {
    @LongConversion(AddressConverter.class)
    private long toAddress;
    private double amount;

    @UsedViaReflection
    public Topup() {
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
