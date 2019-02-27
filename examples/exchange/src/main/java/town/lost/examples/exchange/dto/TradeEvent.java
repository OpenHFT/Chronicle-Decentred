package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.MicroTimestampLongConverter;

public class TradeEvent extends VanillaSignedMessage<TradeEvent> {
    @LongConversion(MicroTimestampLongConverter.class)
    private long orderTimestampUS;
    @LongConversion(AddressLongConverter.class)
    private long orderAddress;
    private double quantity;
    private double price;
    private CurrencyPair currencyPair;
    private Side side;

    public TradeEvent() {
    }

    public TradeEvent(Order order) {
        orderAddress = order.ownerAddress();
        orderTimestampUS = order.ownerOrderTime();
        quantity = order.getQuantity();
        price = order.price();
        side = order.side();
    }

    public long orderTimestampUS() {
        return orderTimestampUS;
    }

    public TradeEvent orderTimestampUS(long orderTimestampUS) {
        this.orderTimestampUS = orderTimestampUS;
        return this;
    }

    public long orderAddress() {
        return orderAddress;
    }

    public TradeEvent orderAddress(long orderAddress) {
        this.orderAddress = orderAddress;
        return this;
    }

    public double quantity() {
        return quantity;
    }

    public TradeEvent quantity(double quantity) {
        this.quantity = quantity;
        return this;
    }

    public double price() {
        return price;
    }

    public TradeEvent price(double price) {
        this.price = price;
        return this;
    }

    public CurrencyPair currencyPair() {
        return currencyPair;
    }

    public TradeEvent currencyPair(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
        return this;
    }

    public Side action() {
        return side;
    }

    public TradeEvent action(Side action) {
        this.side = action;
        return this;
    }
}
