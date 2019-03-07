package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;

public interface VoteTaker {
    void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent);

    boolean hasMajority();

    boolean sendEndOfRoundBlock(long blockNumber);
}
