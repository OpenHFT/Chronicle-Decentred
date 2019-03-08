package net.openhft.chronicle.decentred.server;


import net.openhft.chronicle.decentred.dto.blockevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;

public interface BlockReplayer {

    void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent);

    void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent);

    void replayBlocks();
}
