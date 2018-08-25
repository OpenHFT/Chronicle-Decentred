package net.openhft.chronicle.decentred.dto;

public class TransactionBlockVoteEvent extends VanillaSignedMessage<TransactionBlockVoteEvent> {
    private TransactionBlockGossipEvent gossipEvent;

    public TransactionBlockGossipEvent gossipEvent() {
        if (gossipEvent == null) gossipEvent = new TransactionBlockGossipEvent();
        return gossipEvent;
    }

    public TransactionBlockVoteEvent gossipEvent(TransactionBlockGossipEvent gossipEvent) {
        this.gossipEvent = gossipEvent;
        return this;
    }

    public long chainAddress() {
        return gossipEvent.chainAddress();
    }
}
