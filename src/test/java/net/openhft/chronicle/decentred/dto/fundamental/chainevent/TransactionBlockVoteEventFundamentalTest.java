package net.openhft.chronicle.decentred.dto.fundamental.chainevent;

import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TransactionBlockVoteEventFundamentalTest extends AbstractFundamentalDtoTest<TransactionBlockVoteEvent> {

    private final TransactionBlockGossipEvent transactionBlockGossipEvent = createChild(TransactionBlockGossipEvent.class, TransactionBlockGossipEvent::addressToBlockNumberMap, 37246L);

    TransactionBlockVoteEventFundamentalTest() {
        super(TransactionBlockVoteEvent.class);
    }

    @Override
    protected void initializeSpecifics(TransactionBlockVoteEvent message) {
        message.gossipEvent(transactionBlockGossipEvent);
    }

    @Override
    protected void assertInitializedSpecifics(TransactionBlockVoteEvent message) {
        assertEquals(transactionBlockGossipEvent, message.gossipEvent());
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
