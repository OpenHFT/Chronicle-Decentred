package town.lost.examples.exchange.api;

public interface ExchangeRequests {
    void newOrderCommand(NewOrderCommand newLimitOrderCommand);

    void cancelOrderCommand(CancelOrderCommand cancelOrderCommand);

    void currentBalanceQuery(CurrentBalanceQuery currentBalanceQuery);

    void exchangeRateQuery(ExchangeRateQuery exchangeRateQuery);

}
