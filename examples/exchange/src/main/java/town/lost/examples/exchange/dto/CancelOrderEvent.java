package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressConverter;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.MicroTimestampLongConverter;

public class CancelOrderEvent extends VanillaSignedMessage<CancelOrderEvent> {
    @LongConversion(MicroTimestampLongConverter.class)
    private long orderTimestampUS;
    @LongConversion(AddressConverter.class)
    private long orderAddress;

    public CancelOrderEvent() {
    }

    public CancelOrderEvent(long orderTimestampUS, long orderAddress) {
        this.orderTimestampUS = orderTimestampUS;
        this.orderAddress = orderAddress;
    }

    public long orderTimestampUS() {
        return orderTimestampUS;
    }

    public CancelOrderEvent orderTimestampUS(long orderTimestampUS) {
        this.orderTimestampUS = orderTimestampUS;
        return this;
    }

    public long orderAddress() {
        return orderAddress;
    }

    public CancelOrderEvent orderAddress(long orderAddress) {
        this.orderAddress = orderAddress;
        return this;
    }
}
