package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.wire.AbstractBytesMarshallable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExchangeConfig extends VanillaSignedMessage<ExchangeConfig> {
    private Map<CurrencyPair, CurrencyConfig> currencies = new LinkedHashMap<>();

    public Map<CurrencyPair, CurrencyConfig> currencies() {
        return currencies;
    }

    public static class CurrencyConfig extends AbstractBytesMarshallable {
        private double tickSize;

        public double tickSize() {
            return tickSize;
        }

        public CurrencyConfig tickSize(double tickSize) {
            this.tickSize = tickSize;
            return this;
        }
    }
}
