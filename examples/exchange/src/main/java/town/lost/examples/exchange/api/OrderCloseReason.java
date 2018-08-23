package town.lost.examples.exchange.api;

public enum OrderCloseReason {
    USER_REQUEST((byte) 0), TIME_OUT((byte) 32);

    private byte value;

    OrderCloseReason(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

}
