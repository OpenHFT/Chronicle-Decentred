package town.lost.examples.exchange.api;

import town.lost.examples.exchange.dto.CancelOrderRequest;
import town.lost.examples.exchange.dto.NewOrderRequest;

public interface ExchangeRequests {
    void newOrderRequest(NewOrderRequest newOrderRequest);

    void cancelOrderCommand(CancelOrderRequest cancelOrderRequest);

}
