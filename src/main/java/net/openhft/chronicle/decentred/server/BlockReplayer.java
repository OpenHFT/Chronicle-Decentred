package net.openhft.chronicle.decentred.server;


import net.openhft.chronicle.decentred.dto.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockEvent;

public interface BlockReplayer {
    void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent);

    void treeBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent);

    void replayBlocks();
}
