package net.openhft.chronicle.decentred.server;


import net.openhft.chronicle.decentred.dto.TransactionBlockEvent;

public interface Gossiper {
    void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent);

    void sendGossip(long blockNumber);
}
