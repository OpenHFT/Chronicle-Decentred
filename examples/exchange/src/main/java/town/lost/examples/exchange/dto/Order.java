package town.lost.examples.exchange.dto;

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
        return Comparator.<Order>comparingDouble(o -> -o.price)
                .thenComparingDouble(o -> o.orderId);
    }

    public static Comparator<Order> getSellComparator() {
        return Comparator.<Order>comparingDouble(o -> +o.price)
                .thenComparingDouble(o -> o.orderId);
    }

    public double quantityLeft() {
        return quantity - filled;
    }

    public long expirationTime() {
        return expires;
    }

    public double price() {
        return price;
    }

    public long ownerAddress() {
        return ownerAddress;
    }

    public long ownerOrderTime() {
        return ownerOrderTime;
    }

    public boolean matches(long address, long orderTime) {
        return (ownerAddress == address) && (ownerOrderTime == orderTime);
    }

    public Side side() {
        return side;
    }

    public double getQuantity() {
        return quantity;
    }

    public double fill(double fillQty) {
        assert fillQty <= quantityLeft();
        filled += fillQty;
        return quantityLeft();
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
}
