package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;

public interface Voter {
    void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent);

    void sendVote(long blockNumber);
}
