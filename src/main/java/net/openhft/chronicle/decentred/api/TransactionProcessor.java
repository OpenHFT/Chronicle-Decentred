package net.openhft.chronicle.decentred.api;

public interface TransactionProcessor {
    void blockchainPhase(BlockchainPhase blockchainPhase);

    void messageRouter(MessageRouter messageRouter);
}
