package net.openhft.chronicle.decentred.dto.fundamental.chainevent;

import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public final class TransactionBlockVoteEventFundamentalTest extends AbstractFundamentalDtoTest<TransactionBlockVoteEvent> {

    private static final TransactionBlockGossipEvent TRANSACTION_BLOCK_GOSSIP_EVENT = createChild(TransactionBlockGossipEvent::new, TransactionBlockGossipEvent::addressToBlockNumberMap);

    public TransactionBlockVoteEventFundamentalTest() {
        super(TransactionBlockVoteEvent::new);
    }

    @Override
    protected void initializeSpecifics(TransactionBlockVoteEvent message) {
        message.gossipEvent(TRANSACTION_BLOCK_GOSSIP_EVENT);
    }

    @Override
    protected void assertInitializedSpecifics(TransactionBlockVoteEvent message) {
        assertEquals(TRANSACTION_BLOCK_GOSSIP_EVENT, message.gossipEvent());
    }

    @Override
    protected void assertInitializedToString(String s) {
        assertContains(s, "gossipEvent");
    }

    @Override
    protected Stream<Map.Entry<String, Consumer<TransactionBlockVoteEvent>>> forbiddenAfterSign() {
        return Stream.of(
                entry("gosipEvent", m -> m.gossipEvent(null))
            );
    }

}
