package town.lost.examples.exchange.api;

import net.openhft.chronicle.bytes.MethodId;
import town.lost.examples.exchange.dto.CancelOrderRequest;
import town.lost.examples.exchange.dto.ExchangeConfig;
import town.lost.examples.exchange.dto.NewOrderRequest;
import town.lost.examples.exchange.dto.OpeningBalanceEvent;

public interface ExchangeRequests {
    @MethodId(0x0010)
    void exchangeConfig(ExchangeConfig exchangeConfig);

    @MethodId(0x0020)
    void openningBalanceEvent(OpeningBalanceEvent openingBalanceEvent);

    @MethodId(0x0100)
    void newOrderRequest(NewOrderRequest newOrderRequest);

    @MethodId(0x0101)
    void cancelOrderCommand(CancelOrderRequest cancelOrderRequest);

}
