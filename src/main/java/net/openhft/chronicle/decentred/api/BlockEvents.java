package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.chainevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockVoteEvent;

public interface BlockEvents {

    @MethodId(0xFFF0)
    void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent);

    @MethodId(0xFFF1)
    void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent);

    @MethodId(0xFFF2)
    void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent);

    @MethodId(0xFFF3)
    void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent);

}
