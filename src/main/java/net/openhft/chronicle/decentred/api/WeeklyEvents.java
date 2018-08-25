package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockVoteEvent;

public interface WeeklyEvents {
    // weekly events
    @MethodId(0xFFF0)
    void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent);

    @MethodId(0xFFF1)
    void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent);

    @MethodId(0xFFF2)
    void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent);

    @MethodId(0xFFF3)
    void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent);

}
