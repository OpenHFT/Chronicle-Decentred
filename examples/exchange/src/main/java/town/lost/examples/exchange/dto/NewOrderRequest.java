package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;

public class NewOrderRequest extends VanillaSignedMessage<NewOrderRequest> implements Validable {

    private Side side;
    private double quantity;
    private double maxPrice;
    private CurrencyPair currencyPair;
    private long ttlMillis;

    public NewOrderRequest() {

    }

    public NewOrderRequest(long sourceAddress, long eventTime, Side side, double qty, double maxPrice, CurrencyPair currencyPair, long ttlMillis) {
        this.address(sourceAddress);
        this.timestampUS(eventTime);
        this.side = side;
        this.quantity = qty;
        this.maxPrice = maxPrice;
        this.currencyPair = currencyPair;
        this.ttlMillis = ttlMillis;
    }

    public NewOrderRequest(double quantity, double maxPrice, CurrencyPair currencyPair, long ttlMillis, Side side) {
        this.quantity = quantity;
        this.maxPrice = maxPrice;
        this.currencyPair = currencyPair;
        this.ttlMillis = ttlMillis;
        this.side = side;
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

    public Side side() {
        return side;
    }

    public NewOrderRequest side(Side action) {
        this.side = action;
        return this;
    }

    @Override
    public void validate() throws IllegalStateException {
        if (side == null || currencyPair == null) throw new IllegalStateException();
    }
}
