package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import town.lost.examples.exchange.api.Currency;

import java.util.EnumMap;
import java.util.Map;

public class OpeningBalanceEvent extends VanillaSignedMessage<OpeningBalanceEvent> {
    private Map<Currency, Double> balances = new EnumMap<>(Currency.class);

    public Map<Currency, Double> balances() {
        return balances;
    }
}
