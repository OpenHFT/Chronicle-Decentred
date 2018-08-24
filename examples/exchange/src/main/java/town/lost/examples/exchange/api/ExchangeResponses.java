package town.lost.examples.exchange.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.ApplicationErrorResponse;
import town.lost.examples.exchange.dto.TradeClosedEvent;
import town.lost.examples.exchange.dto.TradeEvent;

public interface ExchangeResponses {
    @MethodId(0x0200)
    void tradeEvent(TradeEvent tradeEvent);

    @MethodId(0x0201)
    void tradeClosedEvent(TradeClosedEvent tradeClosedEvent);

    /**
     * Notify an application error occurred in response to a message passed.
     *
     * @param applicationErrorResponse occurred
     */
    @MethodId(0xFF10)
    void applicationError(ApplicationErrorResponse applicationErrorResponse);
}
