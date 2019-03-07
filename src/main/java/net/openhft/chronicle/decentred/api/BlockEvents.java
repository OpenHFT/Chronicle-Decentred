package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.blockevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;

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
