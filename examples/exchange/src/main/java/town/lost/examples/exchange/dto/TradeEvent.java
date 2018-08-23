package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressConverter;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.MicroTimestampLongConverter;
import town.lost.examples.exchange.api.CurrencyPair;
import town.lost.examples.exchange.api.Side;

public class TradeEvent extends VanillaSignedMessage<TradeEvent> {
    @LongConversion(MicroTimestampLongConverter.class)
    private long orderTimestampUS;
    @LongConversion(AddressConverter.class)
    private long orderAddress;
    private double quantity;
    private double price;
    private CurrencyPair currencyPair;
    private Side action;
    private long orderId;

    public long orderId(){
        return orderId;
    }

    public TradeEvent orderId(long orderId){
        this.orderId = orderId;
        return this;
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
        return action;
    }

    public TradeEvent action(Side action) {
        this.action = action;
        return this;
    }
}
