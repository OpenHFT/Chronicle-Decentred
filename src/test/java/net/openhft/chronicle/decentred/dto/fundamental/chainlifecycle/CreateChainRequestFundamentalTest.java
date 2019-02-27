package net.openhft.chronicle.decentred.dto.fundamental.chainlifecycle;

import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateChainRequest;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CreateChainRequestFundamentalTest extends AbstractFundamentalDtoTest<CreateChainRequest> {

    private static final int CYCLE_OFFSET = 2;
    private static final int ROUNDS_PER_DAY = 24;

    CreateChainRequestFundamentalTest() {
        super(CreateChainRequest::new);
    }

    @Override
    protected void initializeSpecifics(CreateChainRequest message) {
        message.cycleOffset(CYCLE_OFFSET);
        message.roundsPerDay(ROUNDS_PER_DAY);
    }

    @Override
    protected void assertInitializedSpecifics(CreateChainRequest message) {
        assertEquals(CYCLE_OFFSET, message.cycleOffset());
        assertEquals(ROUNDS_PER_DAY, message.roundsPerDay());
    }

    @Override
    protected void assertInitializedToString(String s) {
        assertContains(s, "cycleOffset: +00:00:02");
        assertContains(s, "roundsPerDay: 24");
    }

    @Override
    protected Stream<Map.Entry<String, Consumer<CreateChainRequest>>> forbiddenAfterSign() {
        return Stream.of(
            entry("cycleOffset", m -> m.cycleOffset(1)),
            entry("roundsPerDay", m -> m.roundsPerDay(1))
        );
    }
}
