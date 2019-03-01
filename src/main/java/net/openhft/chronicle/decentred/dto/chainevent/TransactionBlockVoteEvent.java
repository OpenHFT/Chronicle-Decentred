package net.openhft.chronicle.decentred.dto.chainevent;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.decentred.dto.base.TransientFieldHandler;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

// I vote that this gossip event

/**
 * An TransactionBlockVoteEvent is a <em>chain event</em> that holds the gossip events that are in a block.
 *
 */
public final class TransactionBlockVoteEvent extends VanillaSignedMessage<TransactionBlockVoteEvent> {

    private static final String GOSSIP_EVENT = "gossipEvent";

    private transient TransactionBlockGossipEvent gossipEvent;

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


    @Override
    public TransientFieldHandler<TransactionBlockVoteEvent> transientFieldHandler() {
        return TRANSIENTS_HANDLER;
    }

    private static final TransientsHandler TRANSIENTS_HANDLER = new TransientsHandler();

    private static class TransientsHandler implements TransientFieldHandler<TransactionBlockVoteEvent> {

        @Override
        public void reset(@NotNull TransactionBlockVoteEvent original) {
            original.gossipEvent = null;
        }

        @Override
        public void copy(@NotNull TransactionBlockVoteEvent original, @NotNull TransactionBlockVoteEvent target) {
            throw new NotImplementedException();
        }

        @Override
        public void deepCopy(@NotNull TransactionBlockVoteEvent original, @NotNull TransactionBlockVoteEvent target) {
            throw new NotImplementedException();
        }

        @Override
        public void writeMarshallable(@NotNull TransactionBlockVoteEvent original, @NotNull WireOut wire) {
            wire.write(GOSSIP_EVENT).object(
                original.gossipEvent()
            );
        }

        @Override
        public void readMarshallable(@NotNull TransactionBlockVoteEvent original, @NotNull WireIn wire) {
            original.gossipEvent(wire.read(GOSSIP_EVENT).object(TransactionBlockGossipEvent.class));
        }

        @Override
        public void writeMarshallableInternal(@NotNull TransactionBlockVoteEvent original, @NotNull BytesOut bytes) {
            original.gossipEvent().writeMarshallable(bytes);
        }

        @Override
        public void readMarshallable(@NotNull TransactionBlockVoteEvent original, @NotNull BytesIn bytes) {
            original.gossipEvent().readMarshallable(bytes);
        }
    }
}
