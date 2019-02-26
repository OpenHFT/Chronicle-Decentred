package town.lost.examples.appreciation.dto;


import net.openhft.chronicle.core.annotation.UsedViaReflection;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;

/**
 * Automated version of Give that is used to allocate new balances for distribution on a regular basis.
 */
public class Topup extends VanillaSignedMessage<Topup> {
    private double amount;

    @UsedViaReflection
    public Topup() {
    }

    public double amount() {
        return amount;
    }

    public Topup amount(double amount) {
        this.amount = amount;
        return this;
    }
}
