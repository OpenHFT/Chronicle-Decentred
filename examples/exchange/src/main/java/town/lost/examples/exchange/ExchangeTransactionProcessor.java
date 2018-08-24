package town.lost.examples.exchange;

import net.openhft.chronicle.decentred.dto.ApplicationErrorResponse;
import net.openhft.chronicle.decentred.util.LongObjMap;
import town.lost.examples.exchange.api.ExchangeOut;
import town.lost.examples.exchange.api.ExchangeRequests;
import town.lost.examples.exchange.dto.*;

import java.util.EnumMap;
import java.util.Map;

public class ExchangeTransactionProcessor implements ExchangeRequests {
    private final LongObjMap<AccountBalance> accountBalanceMap = LongObjMap.withExpectedSize(AccountBalance.class, 1024);
    private final Map<CurrencyPair, ExchangeMarket> marketMap = new EnumMap<>(CurrencyPair.class);
    private final ExchangeOut out;

    public ExchangeTransactionProcessor(ExchangeOut out) {
        this.out = out;
    }

    @Override
    public void exchangeConfig(ExchangeConfig exchangeConfig) {
        for (Map.Entry<CurrencyPair, ExchangeConfig.CurrencyConfig> entry : exchangeConfig.currencies().entrySet()) {
            CurrencyPair currencyPair = entry.getKey();
            marketMap.put(currencyPair,
                    new ExchangeMarket(entry.getValue().tickSize(),
                            (a, i, q) -> onTrade(currencyPair, a, i, q),
                            (o, r) -> onClose(currencyPair, o, r)));
        }
    }

    private void onTrade(CurrencyPair currencyPair, Order aggressive, Order initiator, double qty) {
        out.to(aggressive.ownerAddress())
                .tradeEvent(new TradeEvent(aggressive)
                        .currencyPair(currencyPair)
                        .quantity(qty));
        out.to(initiator.ownerAddress())
                .tradeEvent(new TradeEvent(initiator)
                        .currencyPair(currencyPair)
                        .quantity(qty));
    }

    private void onClose(CurrencyPair currencyPair, Order order, OrderCloseReason orderCloseReason) {
        out.to(order.ownerAddress())
                .tradeClosedEvent(new TradeClosedEvent(order)
                        .currencyPair(currencyPair)
                        .orderCloseReason(orderCloseReason));
    }

    @Override
    public void openningBalanceEvent(OpeningBalanceEvent openingBalanceEvent) {
        long account = openingBalanceEvent.balanceAddress();
        if (accountBalanceMap.containsKey(account)) {
            out.applicationError(new ApplicationErrorResponse().init(openingBalanceEvent, "Account already set"));
            return;
        }
        accountBalanceMap.justPut(account, new AccountBalance(openingBalanceEvent));
    }

    @Override
    public void newOrderRequest(NewOrderRequest newOrderRequest) {
        ExchangeMarket exchangeMarket = marketMap.get(newOrderRequest.currencyPair());
        exchangeMarket.executeOrder(newOrderRequest);
    }

    @Override
    public void cancelOrderCommand(CancelOrderRequest cancelOrderRequest) {
        ExchangeMarket exchangeMarket = marketMap.get(cancelOrderRequest.currencyPair());
        exchangeMarket.cancelOrder(cancelOrderRequest.address(), cancelOrderRequest.orderTimestampUS());
    }
}
