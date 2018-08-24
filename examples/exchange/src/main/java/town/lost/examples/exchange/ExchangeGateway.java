package town.lost.examples.exchange;

import net.openhft.chronicle.decentred.api.MessageRouter;
import town.lost.examples.exchange.api.ExchangeRequests;
import town.lost.examples.exchange.api.ExchangeResponses;
import town.lost.examples.exchange.dto.CancelOrderRequest;
import town.lost.examples.exchange.dto.ExchangeConfig;
import town.lost.examples.exchange.dto.NewOrderRequest;
import town.lost.examples.exchange.dto.OpeningBalanceEvent;

public class ExchangeGateway implements ExchangeRequests {
    private final MessageRouter<ExchangeResponses> router;
    private final ExchangeRequests blockchain;

    public ExchangeGateway(MessageRouter<ExchangeResponses> router, ExchangeRequests blockchain) {
        this.router = router;
        this.blockchain = blockchain;
    }

    @Override
    public void exchangeConfig(ExchangeConfig exchangeConfig) {
        blockchain.exchangeConfig(exchangeConfig);
    }

    @Override
    public void openningBalanceEvent(OpeningBalanceEvent openingBalanceEvent) {
        openingBalanceEvent.validate();
        blockchain.openningBalanceEvent(openingBalanceEvent);
    }

    @Override
    public void newOrderRequest(NewOrderRequest newOrderRequest) {
        newOrderRequest.validate();
        blockchain.newOrderRequest(newOrderRequest);
    }

    @Override
    public void cancelOrderCommand(CancelOrderRequest cancelOrderRequest) {
        cancelOrderRequest.validate();
        blockchain.cancelOrderCommand(cancelOrderRequest);
    }
}
