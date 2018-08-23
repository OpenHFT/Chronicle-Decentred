package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import town.lost.examples.exchange.api.CurrencyPair;
import town.lost.examples.exchange.api.Side;

public class NewOrderRequest extends VanillaSignedMessage<NewOrderRequest> {

    private Side action;
    private double quantity;
    private double maxPrice;
    private CurrencyPair currencyPair;
    private long ttlMillis;

    public NewOrderRequest() {

    }

    public NewOrderRequest(long sourceAddress, long eventTime, Side action, double qty, double maxPrice, CurrencyPair currencyPair, long ttlMillis){
        this.address(sourceAddress);
        this.timestampUS(eventTime);
        this.action = action;
        this.quantity = qty;
        this.maxPrice = maxPrice;
        this.currencyPair = currencyPair;
        this.ttlMillis = ttlMillis;
    }

    public NewOrderRequest(double quantity, double maxPrice, CurrencyPair currencyPair, long ttlMillis, Side action) {
        this.quantity = quantity;
        this.maxPrice = maxPrice;
        this.currencyPair = currencyPair;
        this.ttlMillis = ttlMillis;
        this.action = action;
    }

    public double quantity() {
        return quantity;
    }

    public NewOrderRequest quantity(double quantity) {
        this.quantity = quantity;
        return this;
    }

    public double maxPrice() {
        return maxPrice;
    }

    public NewOrderRequest maxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
        return this;
    }

    public CurrencyPair currencyPair() {
        return currencyPair;
    }

    public NewOrderRequest currencyPair(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
        return this;
    }

    public long ttlMillis() {
        return ttlMillis;
    }

    public NewOrderRequest ttlMillis(long ttlMillis) {
        this.ttlMillis = ttlMillis;
        return this;
    }

    public Side action() {
        return action;
    }

    public NewOrderRequest action(Side action) {
        this.action = action;
        return this;
    }
}
