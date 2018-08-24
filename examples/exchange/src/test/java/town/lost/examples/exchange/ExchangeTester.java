package town.lost.examples.exchange;

import town.lost.examples.exchange.api.ExchangeOut;
import town.lost.examples.exchange.api.ExchangeRequests;
import town.lost.examples.exchange.api.ExchangeResponses;

interface ExchangeTester extends ExchangeRequests, ExchangeOut, ExchangeResponses {
    void setCurrentTime(long currentTimeMillis);
}
