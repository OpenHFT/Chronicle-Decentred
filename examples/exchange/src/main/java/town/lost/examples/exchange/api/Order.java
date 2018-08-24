package town.lost.examples.exchange.api;

import net.openhft.chronicle.wire.AbstractMarshallable;

import java.util.Comparator;

public class Order extends AbstractMarshallable {

    private final long ownerAddress;
    private final long ownerOrderTime;
    private final long orderId;
    private final long expires; // millis
    private final double quantity;
    private final double price;
    private final Side side;
    private long filled = 0;

    public Order(long orderId, Side side, double quantity, double price, long expires, long ownerAddress, long ownerOrderTime) {
        this.orderId = orderId;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.expires = expires;
        this.ownerAddress = ownerAddress;
        this.ownerOrderTime = ownerOrderTime;
    }

    public static Comparator<Order> getBuyComparator() {
        return new PriceComparator().reversed().thenComparingDouble(o -> o.orderId);
    }

    public static Comparator<Order> getSellComparator() {
        return new PriceComparator().thenComparingDouble(o -> o.orderId);
    }

    public double getQuantityLeft() {
        return quantity - filled;
    }

    public long getExpirationTime() {
        return expires;
    }

    public double getPrice() {
        return price;
    }

    public long getOrderId() {
        return orderId;
    }

    public long getOwnerAddress() {
        return ownerAddress;
    }

    public long getOwnerOrderTime() {
        return ownerOrderTime;
    }

    public boolean matches(long address, long orderTime) {
        return (ownerAddress == address) && (ownerOrderTime == orderTime);
    }

    public Side getSide() {
        return side;
    }

    public double getQuantity() {
        return quantity;
    }

    public double fill(double fillQty) {
        assert fillQty <= getQuantityLeft();
        filled += fillQty;
        return getQuantityLeft();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(orderId);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj instanceof Order;
        return (this == obj) || (orderId == ((Order) obj).orderId);
    }

    private static class PriceComparator implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            assert (o1 != null) && (o2 != null);
            return Double.compare(o1.price, o2.price);
        }
    }
}
