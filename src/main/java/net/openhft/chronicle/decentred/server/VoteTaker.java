package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.server.trait.HasTcpMessageListener;
import org.jetbrains.annotations.NotNull;

public interface VoteTaker extends HasTcpMessageListener {

    void transactionBlockVoteEvent(@NotNull TransactionBlockVoteEvent transactionBlockVoteEvent);

    boolean hasMajority();

    boolean sendEndOfRoundBlock(long blockNumber);
}
