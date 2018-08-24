package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressConverter;
import net.openhft.chronicle.wire.LongConversion;

import java.util.EnumMap;
import java.util.Map;

public class OpeningBalanceEvent extends VanillaSignedMessage<OpeningBalanceEvent> {
    @LongConversion(AddressConverter.class)
    private long balanceAddress;
    private final Map<Currency, Double> balances = new EnumMap<>(Currency.class);

    public Map<Currency, Double> balances() {
        return balances;
    }

    public long balanceAddress() {
        return balanceAddress;
    }

    public OpeningBalanceEvent balanceAddress(long balanceAccount) {
        this.balanceAddress = balanceAccount;
        return this;
    }
}
