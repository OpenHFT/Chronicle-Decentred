package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.dto.TransactionBlockVoteEvent;

public interface VoteTaker {
    void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent);

    boolean hasMajority();

    void sendEndOfRoundBlock(long blockNumber);
}
