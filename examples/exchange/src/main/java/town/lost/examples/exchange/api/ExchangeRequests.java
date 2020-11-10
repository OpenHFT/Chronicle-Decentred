package town.lost.examples.exchange.api;

import net.openhft.chronicle.bytes.MethodId;
import town.lost.examples.exchange.dto.*;

public interface ExchangeRequests {
    @MethodId(0x0010)
    void exchangeConfig(ExchangeConfig exchangeConfig);

    @MethodId(0x0011)
    void exchangeCloseRequest(ExchangeCloseRequest exchangeCloseRequest);

    @MethodId(0x0020)
    void openningBalanceEvent(OpeningBalanceEvent openingBalanceEvent);

    @MethodId(0x0100)
    void newOrderRequest(NewOrderRequest newOrderRequest);

    @MethodId(0x0101)
    void cancelOrderCommand(CancelOrderRequest cancelOrderRequest);

}
