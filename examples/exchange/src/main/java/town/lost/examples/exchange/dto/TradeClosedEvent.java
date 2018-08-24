package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressConverter;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.MicroTimestampLongConverter;

public class TradeClosedEvent extends VanillaSignedMessage<TradeClosedEvent> {
    @LongConversion(MicroTimestampLongConverter.class)
    private long orderTimestampUS;
    @LongConversion(AddressConverter.class)
    private long orderAddress;
    private CurrencyPair currencyPair;
    private Side side;
    private OrderCloseReason orderCloseReason;

    public TradeClosedEvent(Order order) {
        orderAddress = order.ownerAddress();
        orderTimestampUS = order.ownerOrderTime();
        side = order.side();
    }

    public long orderTimestampUS() {
        return orderTimestampUS;
    }

    public TradeClosedEvent orderTimestampUS(long orderTimestampUS) {
        this.orderTimestampUS = orderTimestampUS;
        return this;
    }

    public long orderAddress() {
        return orderAddress;
    }

    public TradeClosedEvent orderAddress(long orderAddress) {
        this.orderAddress = orderAddress;
        return this;
    }

    public CurrencyPair currencyPair() {
        return currencyPair;
    }

    public TradeClosedEvent currencyPair(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
        return this;
    }

    public Side side() {
        return side;
    }

    public TradeClosedEvent side(Side side) {
        this.side = side;
        return this;
    }

    public OrderCloseReason orderCloseReason() {
        return orderCloseReason;
    }

    public TradeClosedEvent orderCloseReason(OrderCloseReason orderCloseReason) {
        this.orderCloseReason = orderCloseReason;
        return this;
    }
}
