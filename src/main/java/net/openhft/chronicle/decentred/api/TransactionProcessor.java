package net.openhft.chronicle.decentred.api;

public interface TransactionProcessor {
    void messageRouter(MessageRouter messageRouter);
}
