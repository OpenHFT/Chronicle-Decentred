package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockGossipEvent;

public interface Voter {
    void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent);

    void sendVote(long blockNumber);
}
