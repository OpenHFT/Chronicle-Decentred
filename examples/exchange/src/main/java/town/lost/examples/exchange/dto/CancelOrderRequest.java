package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.MicroTimestampLongConverter;

public class CancelOrderRequest extends VanillaSignedMessage<CancelOrderRequest> implements Validable {

    private CurrencyPair currencyPair;
    @LongConversion(MicroTimestampLongConverter.class)
    private long orderTimestampUS;

    public CancelOrderRequest() {

    }

    public long orderTimestampUS() {
        return orderTimestampUS;
    }

    public CancelOrderRequest orderTimestampUS(long orderTimestampUS) {
        this.orderTimestampUS = orderTimestampUS;
        return this;
    }

    public CurrencyPair currencyPair() {
        return currencyPair;
    }

    public CancelOrderRequest currencyPair(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
        return this;
    }

    public void validate() {
        if (currencyPair == null) throw new IllegalStateException();
    }
}
