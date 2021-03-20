package town.lost.examples.exchange.dto;

public enum OrderCloseReason {
    USER_REQUEST(0),
    UNKNOWN_SYMBOL(1),
    EXCHANGE_CLOSED(2),
    TIME_OUT(3);

    private final byte value;

    OrderCloseReason(int value) {
        this.value = (byte) value;
    }

    public byte value() {
        return value;
    }
}
