package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import town.lost.examples.exchange.api.Currency;

import java.util.EnumMap;

public class OpeningBalance extends VanillaSignedMessage<OpeningBalance> {
    private EnumMap<Currency, Double> balances = new EnumMap<>(Currency.class);

    public EnumMap<Currency, Double> balances() {
        return balances;
    }
}
