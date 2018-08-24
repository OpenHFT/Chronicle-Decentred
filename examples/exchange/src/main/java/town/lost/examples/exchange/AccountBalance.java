package town.lost.examples.exchange;

import town.lost.examples.exchange.dto.Currency;
import town.lost.examples.exchange.dto.OpeningBalanceEvent;

import java.util.EnumMap;
import java.util.Map;

public class AccountBalance {

    private final EnumMap<Currency, CurrencyBalance> currencyMap = new EnumMap<>(Currency.class);

    public AccountBalance(OpeningBalanceEvent openingBalanceEvent) {
        for (Map.Entry<Currency, Double> entry : openingBalanceEvent.balances().entrySet()) {
            currencyMap.put(entry.getKey(), new CurrencyBalance().available(entry.getValue()));
        }
    }

    static class CurrencyBalance {
        double available;
        double reserved;

        public double available() {
            return available;
        }

        public CurrencyBalance available(double available) {
            this.available = available;
            return this;
        }

        public double reserved() {
            return reserved;
        }

        public CurrencyBalance reserved(double reserved) {
            this.reserved = reserved;
            return this;
        }
    }
}
