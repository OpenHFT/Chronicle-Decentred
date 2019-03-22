package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.decentred.util.DtoRegistry;

public class TransactionBlockVoteEvent extends VanillaSignedMessage<TransactionBlockVoteEvent> {
    private transient DtoRegistry dtoRegistry;
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

    @Override
    public TransactionBlockVoteEvent dtoRegistry(DtoRegistry dtoRegistry) {
        this.dtoRegistry = dtoRegistry;
        return this;
    }

    @Override
    public TransactionBlockVoteEvent sign(BytesStore secretKey, TimeProvider timeProvider) {
        if (!gossipEvent.signed()) {
            gossipEvent.protocol(dtoRegistry.protocolFor(gossipEvent.getClass()));
            gossipEvent.messageType(dtoRegistry.messageTypeFor(gossipEvent.getClass()));
            gossipEvent.sign(secretKey, timeProvider);
        }
        return super.sign(secretKey, timeProvider);
    }
}
