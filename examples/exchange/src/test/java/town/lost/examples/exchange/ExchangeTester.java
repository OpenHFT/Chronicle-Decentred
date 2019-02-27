package town.lost.examples.exchange;

import net.openhft.chronicle.decentred.dto.error.ApplicationErrorResponse;
import town.lost.examples.exchange.api.ExchangeOut;
import town.lost.examples.exchange.api.ExchangeRequests;
import town.lost.examples.exchange.api.ExchangeResponses;

interface ExchangeTester extends ExchangeRequests, ExchangeOut, ExchangeResponses {
    void setCurrentTime(long currentTimeMillis);

    @Override
    default void applicationError(ApplicationErrorResponse applicationErrorResponse) {
        ExchangeOut.super.applicationError(applicationErrorResponse);
    }
}
