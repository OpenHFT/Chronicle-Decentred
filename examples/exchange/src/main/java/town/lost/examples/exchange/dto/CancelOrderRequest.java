package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;

public class CancelOrderRequest extends VanillaSignedMessage<CancelOrderRequest> {

    private long orderTimestampUS;

    public CancelOrderRequest(long orderTimestampUS) {
        this.orderTimestampUS = orderTimestampUS;
    }

    public CancelOrderRequest() {

    }

    public long orderTimestampUS() {
        return orderTimestampUS;
    }

    public CancelOrderRequest orderTimestampUS(long orderTimestampUS) {
        this.orderTimestampUS = orderTimestampUS;
        return this;
    }
}
