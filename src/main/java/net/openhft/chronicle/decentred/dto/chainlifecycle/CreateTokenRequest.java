package net.openhft.chronicle.decentred.dto.chainlifecycle;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.wire.Base85LongConverter;
import net.openhft.chronicle.wire.LongConversion;

// Once a chain then create a token associated to the chaine. Any number N
// The "type" of the chain.
// Shares, Permissions, Fixed value chains
public final class CreateTokenRequest extends VanillaSignedMessage<CreateTokenRequest> {

    @LongConversion(Base85LongConverter.class)
    private long symbol;   // 1..10 characters
    private double amount; // How much this chain can hold like 10M MyMoney
    private double granularity; // 0.01 e.g. cents

    public long symbol() {
        return symbol;
    }

    // At least X characters, where X is perhaps 1
    public CreateTokenRequest symbol(long symbol) {
        assertNotSigned();
        this.symbol = symbol;
        return this;
    }

    public double amount() {
        return amount;
    }

    public CreateTokenRequest amount(double amount) {
        assertNotSigned();
        this.amount = amount;
        return this;
    }

    public double granularity() {
        return granularity;
    }

    public CreateTokenRequest granularity(double granularity) {
        assertNotSigned();
        if (granularity < 0.0) {
            throw new IllegalArgumentException("Granularity must be positive: " + granularity);
        }

        this.granularity = granularity;
        return this;
    }

}
