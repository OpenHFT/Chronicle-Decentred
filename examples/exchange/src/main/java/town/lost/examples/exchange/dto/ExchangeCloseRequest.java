package town.lost.examples.exchange.dto;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;

public class ExchangeCloseRequest extends VanillaSignedMessage<ExchangeCloseRequest> {
    String reason;

    public String reason() {
        return reason;
    }

    public ExchangeCloseRequest reason(String reason) {
        this.reason = reason;
        return this;
    }
}
