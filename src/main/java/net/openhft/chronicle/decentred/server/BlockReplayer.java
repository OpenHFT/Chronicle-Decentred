package net.openhft.chronicle.decentred.server;


import net.openhft.chronicle.decentred.dto.chainevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockEvent;

public interface BlockReplayer {
    void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent);

    void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent);

    void replayBlocks();
}
