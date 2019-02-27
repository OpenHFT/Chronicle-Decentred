package net.openhft.chronicle.decentred.dto.chainevent;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;

// I vote that this gossip event
// Currently: It votes on the last piece of gossip

/**
 * An TransactionBlockVoteEvent is a <em>chain event</em> that holds the gossip events that are in a block.
 *
 */
public class TransactionBlockVoteEvent extends VanillaSignedMessage<TransactionBlockVoteEvent> {
    private TransactionBlockGossipEvent gossipEvent;

    public TransactionBlockGossipEvent gossipEvent() {
        if (gossipEvent == null) gossipEvent = new TransactionBlockGossipEvent();
        return gossipEvent;
    }

    public TransactionBlockVoteEvent gossipEvent(TransactionBlockGossipEvent gossipEvent) {
        assertNotSigned();
        this.gossipEvent = gossipEvent;
        return this;
    }

    public long chainAddress() {
        return gossipEvent.chainAddress();
    }
}
