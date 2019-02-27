package town.lost.examples.exchange;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.dto.error.ApplicationErrorResponse;
import town.lost.examples.exchange.api.ExchangeRequests;
import town.lost.examples.exchange.api.ExchangeResponses;
import town.lost.examples.exchange.dto.*;

public class ExchangeGateway implements ExchangeRequests {
    private final MessageRouter<ExchangeResponses> router;
    private final ExchangeRequests blockchain;

    public ExchangeGateway(MessageRouter<ExchangeResponses> router, ExchangeRequests blockchain) {
        this.router = router;
        this.blockchain = blockchain;
    }

    protected boolean privilegedAddress(long address) {
        return true;
    }

    @Override
    public void exchangeConfig(ExchangeConfig exchangeConfig) {
        if (!privilegedAddress(exchangeConfig.address())) {
            router.to(0).applicationError(new ApplicationErrorResponse().init(exchangeConfig, "Not authorized"));
            return;
        }
        blockchain.exchangeConfig(exchangeConfig);
    }

    @Override
    public void openningBalanceEvent(OpeningBalanceEvent openingBalanceEvent) {
        if (!privilegedAddress(openingBalanceEvent.address())) {
            router.to(0).applicationError(new ApplicationErrorResponse().init(openingBalanceEvent, "Not authorized"));
            return;
        }
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

    @Override
    public void exchangeCloseRequest(ExchangeCloseRequest exchangeCloseRequest) {
        if (!privilegedAddress(exchangeCloseRequest.address())) {
            router.to(0).applicationError(new ApplicationErrorResponse().init(exchangeCloseRequest, "Not authorized"));
            return;
        }
        blockchain.exchangeCloseRequest(exchangeCloseRequest);
    }
}
