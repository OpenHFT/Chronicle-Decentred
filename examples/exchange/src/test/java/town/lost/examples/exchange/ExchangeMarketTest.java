package town.lost.examples.exchange;


import net.openhft.chronicle.core.Mocker;
import org.junit.Test;
import town.lost.examples.exchange.ExchangeMarket.OrderClosedListener;
import town.lost.examples.exchange.ExchangeMarket.TradeListener;
import town.lost.examples.exchange.dto.*;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.Assert.*;
import static town.lost.examples.exchange.dto.Side.BUY;
import static town.lost.examples.exchange.dto.Side.SELL;

public class ExchangeMarketTest {


    @Test
    public void simpleOrdersMatch() {
        CurrencyPair currencyPair = CurrencyPair.USDXCL;
        Queue<MethodCall> resultsQueue = new ArrayDeque<>();
        TradeListener tradeListener = Mocker.intercepting(TradeListener.class,
                (name, params) -> resultsQueue.add(new MethodCall(name, params)), null);
        OrderClosedListener closedListener = Mocker.ignored(OrderClosedListener.class);
        int tickSize = 1;
        double precision = Side.getDefaultPrecision(tickSize);
        try (ExchangeMarket market = new ExchangeMarket(tickSize, tradeListener, closedListener)) {
            market.setCurrentTime(101);
            market.executeOrder(new NewOrderRequest(1, 100, BUY, 100, 200, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 100, BUY, 100, 199, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 101, BUY, 100, 180, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 102, SELL, 100, 210, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 103, SELL, 100, 230, currencyPair, 100000));

            assertEquals(3, market.getOrdersCount(BUY));
            assertEquals(2, market.getOrdersCount(SELL));
            assertTrue(resultsQueue.isEmpty());
            market.setCurrentTime(115);
            market.executeOrder(new NewOrderRequest(1, 120, BUY, 100, 210, currencyPair, 100000));
            assertFalse(resultsQueue.isEmpty());
            MethodCall call = resultsQueue.poll();
            Order aggressor = call.getParams(0);
            assertEquals(BUY, aggressor.side());
            assertEquals(0L, aggressor.quantityLeft(), 0);
            assertEquals(aggressor.getQuantity(), aggressor.quantityLeft() + (Double) call.getParams(2), 0);
            assertEquals(BUY, aggressor.side());
            Order initiator = call.getParams(1);
            assertEquals(SELL, initiator.side());
            assertEquals(210D, initiator.price(), precision);
            assertEquals(0L, initiator.quantityLeft(), 0);
            assertEquals(100L, call.<Double>getParams(2).longValue());
            assertEquals(initiator.getQuantity(), initiator.quantityLeft() + (Double) call.getParams(2), 0);

            assertNotEquals(aggressor, initiator);

            assertEquals(3, market.getOrdersCount(BUY));
            assertEquals(1, market.getOrdersCount(SELL));

            market.setCurrentTime(215);
            market.executeOrder(new NewOrderRequest(1, 300, SELL, 300, 190, currencyPair, 100000));
            assertEquals(2, resultsQueue.size());
            assertEquals(1, market.getOrdersCount(BUY));
            assertEquals(2, market.getOrdersCount(SELL));

            call = resultsQueue.poll();
            assert call != null;
            aggressor = call.getParams(0);
            assertEquals(SELL, aggressor.side());
            assertEquals(200L, aggressor.quantityLeft(), 0);
            initiator = call.getParams(1);
            assertEquals(BUY, initiator.side());
            assertEquals(200D, initiator.price(), precision);
            assertEquals(100.0, call.getParams()[2]);
            assertNotEquals(aggressor, initiator);

            call = resultsQueue.poll();
            aggressor = call.getParams(0);
            assertEquals(SELL, aggressor.side());
            assertEquals(100L, aggressor.quantityLeft(), 0);
            initiator = call.getParams(1);
            assertEquals(BUY, initiator.side());
            assertEquals(199D, initiator.price(), precision);
            assertEquals(100.0, call.getParams()[2]);
            assertNotEquals(aggressor, initiator);
        }
    }

    @Test
    public void simpleOrdersMatch2() {
        TestUtils.test("orders/simpleMatch");
    }

    @Test
    public void orderWitZeroTtl() {
        CurrencyPair currencyPair = CurrencyPair.EURXCL;
        Queue<MethodCall> resultsQueue = new ArrayDeque<>();
        TradeListener tradeListener = Mocker.intercepting(TradeListener.class,
                (name, params) -> resultsQueue.add(new MethodCall(name, params)), null);
        OrderClosedListener closedListener = Mocker.ignored(OrderClosedListener.class);
        int tickSize = 1;
        double precision = Side.getDefaultPrecision(tickSize);
        try (ExchangeMarket market = new ExchangeMarket(tickSize, tradeListener, closedListener)) {
            market.setCurrentTime(101);
            market.executeOrder(new NewOrderRequest(1, 100, BUY, 100, 200, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 100, BUY, 100, 199, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 101, BUY, 100, 180, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 102, SELL, 100, 210, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 103, SELL, 100, 230, currencyPair, 100000));

            assertEquals(3, market.getOrdersCount(BUY));
            assertEquals(2, market.getOrdersCount(SELL));
            market.setCurrentTime(115);
            market.executeOrder(new NewOrderRequest(1, 120, BUY, 100, 205, currencyPair, 0));
            // no fills no new order
            assertTrue(resultsQueue.isEmpty());
            assertEquals(3, market.getOrdersCount(BUY));
            assertEquals(2, market.getOrdersCount(SELL));

            market.setCurrentTime(120);
            market.executeOrder(new NewOrderRequest(1, 120, SELL, 200, 200, currencyPair, 0));
            // some fills but no new sell order on the market
            assertFalse(resultsQueue.isEmpty());
            MethodCall call = resultsQueue.poll();
            Order aggressor = call.getParams(0);
            assertEquals(SELL, aggressor.side());
            assertEquals(100L, aggressor.quantityLeft(), 0);
            Order initiator = call.getParams(1);
            assertEquals(BUY, initiator.side());
            assertEquals(200D, initiator.price(), precision);
            assertEquals(0L, initiator.quantityLeft(), 0);
            assertEquals(100.0, call.getParams()[2]);

            assertEquals(2, market.getOrdersCount(BUY));
            assertEquals(2, market.getOrdersCount(SELL));


        }
    }


    @Test
    public void userCancel() {
        CurrencyPair currencyPair = CurrencyPair.EURXCL;
        TradeListener tradeListener = Mocker.ignored(TradeListener.class);
        Queue<MethodCall> resultsQueue = new ArrayDeque<>();
        OrderClosedListener closedListener = Mocker.intercepting(OrderClosedListener.class,
                (name, params) -> resultsQueue.add(new MethodCall(name, params)), null);
        try (ExchangeMarket market = new ExchangeMarket(1, tradeListener, closedListener)) {
            market.setCurrentTime(101);
            market.executeOrder(new NewOrderRequest(1, 100, BUY, 111, 200, currencyPair, 1000));
            assertEquals(1, market.getOrdersCount(BUY));
            market.setCurrentTime(105);
            market.cancelOrder(1, 100);

            assertTrue(!resultsQueue.isEmpty());
            MethodCall call = resultsQueue.poll();
            Order order = call.getParams(0);
            assertEquals(1, order.ownerAddress());
            assertEquals(100, order.ownerOrderTime());
            assertEquals(OrderCloseReason.USER_REQUEST, call.getParams(1));
            assertEquals(0, market.getOrdersCount(BUY));

            market.setCurrentTime(205);
            market.executeOrder(new NewOrderRequest(1, 200, SELL, 111, 200, currencyPair, 1000));
            assertEquals(1, market.getOrdersCount(SELL));
            market.setCurrentTime(210);
            market.cancelOrder(1, 200);

            assertTrue(!resultsQueue.isEmpty());
            call = resultsQueue.poll();
            order = call.getParams(0);
            assertEquals(1, order.ownerAddress());
            assertEquals(200, order.ownerOrderTime());
            assertEquals(OrderCloseReason.USER_REQUEST, call.getParams(1));
            assertEquals(0, market.getOrdersCount(BUY));

        }
    }

    @Test
    public void cancelUnexistingOrder() {
        CurrencyPair currencyPair = CurrencyPair.EURXCL;
        TradeListener tradeListener = Mocker.ignored(TradeListener.class);
        Queue<MethodCall> resultsQueue = new ArrayDeque<>();
        OrderClosedListener closedListener = Mocker.intercepting(OrderClosedListener.class,
                (name, params) -> resultsQueue.add(new MethodCall(name, params)), null);
        try (ExchangeMarket market = new ExchangeMarket(1, tradeListener, closedListener)) {
            market.setCurrentTime(101);
            market.executeOrder(new NewOrderRequest(1, 100, BUY, 111, 200, currencyPair, 1000));
            market.executeOrder(new NewOrderRequest(1, 200, SELL, 111, 400, currencyPair, 1000));
            assertEquals(1, market.getOrdersCount(BUY));
            assertEquals(1, market.getOrdersCount(SELL));

            market.setCurrentTime(105);
            market.cancelOrder(2, 100);
            assertTrue(resultsQueue.isEmpty());
            market.cancelOrder(1, 300);
            assertTrue(resultsQueue.isEmpty());
            assertEquals(1, market.getOrdersCount(BUY));
            assertEquals(1, market.getOrdersCount(SELL));
        }
    }

    @Test
    public void timeoutCancel() {
        CurrencyPair currencyPair = CurrencyPair.EURXCL;
        TradeListener tradeListener = Mocker.ignored(TradeListener.class);
        Queue<MethodCall> resultsQueue = new ArrayDeque<>();
        OrderClosedListener closedListener = Mocker.intercepting(OrderClosedListener.class,
                (name, params) -> resultsQueue.add(new MethodCall(name, params)), null);
        try (ExchangeMarket market = new ExchangeMarket(1, tradeListener, closedListener)) {
            market.setCurrentTime(100);
            market.executeOrder(new NewOrderRequest(1, 100, BUY, 111, 200.0, currencyPair, 1400));
            market.executeOrder(new NewOrderRequest(1, 101, BUY, 111, 200.0, currencyPair, 1000));
            market.executeOrder(new NewOrderRequest(1, 102, BUY, 111, 200.0, currencyPair, 10000));
            market.setCurrentTime(200);
            market.executeOrder(new NewOrderRequest(1, 201, SELL, 111, 201.0, currencyPair, 1300));
            market.executeOrder(new NewOrderRequest(1, 202, SELL, 111, 201.0, currencyPair, 1000));
            market.executeOrder(new NewOrderRequest(1, 203, SELL, 111, 201.0, currencyPair, 10000));
            market.setCurrentTime(1500);
            assertEquals(3, market.getOrdersCount(BUY));
            assertEquals(3, market.getOrdersCount(SELL));
            market.removeExpired();
            assertEquals(4, resultsQueue.size());
            while (!resultsQueue.isEmpty()) {
                MethodCall call = resultsQueue.poll();
                Order order = call.getParams(0);
                assertEquals(1, order.ownerAddress());
                assertEquals(OrderCloseReason.TIME_OUT, call.getParams(1));
            }
            assertEquals(1, market.getOrdersCount(BUY));
            assertEquals(1, market.getOrdersCount(SELL));
        }
    }

    @Test
    public void timeoutCancelTopWhileMatch() {
        CurrencyPair currencyPair = CurrencyPair.EURXCL;
        TradeListener tradeListener = Mocker.ignored(TradeListener.class);
        Queue<MethodCall> resultsQueue = new ArrayDeque<>();
        OrderClosedListener closedListener = Mocker.intercepting(OrderClosedListener.class,
                (name, params) -> resultsQueue.add(new MethodCall(name, params)), null);
        try (ExchangeMarket market = new ExchangeMarket(1, tradeListener, closedListener)) {
            market.setCurrentTime(100);
            market.executeOrder(new NewOrderRequest(1, 100, BUY, 111, 203.0, currencyPair, 1400));
            market.executeOrder(new NewOrderRequest(1, 101, BUY, 111, 202.0, currencyPair, 10000));
            market.executeOrder(new NewOrderRequest(1, 102, BUY, 111, 201.0, currencyPair, 10000));
            assertEquals(3, market.getOrdersCount(BUY));
            assertEquals(0, market.getOrdersCount(SELL));
            market.setCurrentTime(1500);
            market.executeOrder(new NewOrderRequest(1, 1000, SELL, 111, 220.0, currencyPair, 10000));

            assertEquals(1, resultsQueue.size());
            MethodCall call = resultsQueue.poll();
            Order order = call.getParams(0);
            assertEquals(1, order.ownerAddress());
            assertEquals(OrderCloseReason.TIME_OUT, call.getParams(1));
            assertEquals(2, market.getOrdersCount(BUY));
            assertEquals(1, market.getOrdersCount(SELL));
        }
    }

    @Test
    public void checkClose() {
        CurrencyPair currencyPair = CurrencyPair.EURXCL;
        TradeListener tradeListener = Mocker.ignored(TradeListener.class);
        Queue<MethodCall> resultsQueue = new ArrayDeque<>();
        OrderClosedListener closedListener = Mocker.intercepting(OrderClosedListener.class,
                (name, params) -> resultsQueue.add(new MethodCall(name, params)), null);
        int tickSize = 1;
        ExchangeMarket market = new ExchangeMarket(tickSize, tradeListener, closedListener);
        try {
            market.setCurrentTime(101);
            market.executeOrder(new NewOrderRequest(1, 100, BUY, 100, 200, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 100, BUY, 100, 199, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 101, BUY, 100, 180, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 102, SELL, 100, 210, currencyPair, 100000));
            market.executeOrder(new NewOrderRequest(1, 103, SELL, 100, 230, currencyPair, 100000));
            market.setCurrentTime(1001);
        } finally {
            market.close();
        }
        assertEquals(0, market.getOrdersCount(BUY));
        assertEquals(0, market.getOrdersCount(SELL));
        market.removeExpired();
        assertEquals(5, resultsQueue.size());
    }
}
