package net.openhft.chronicle.decentred.dto.fundamental.chainlifecycle;

import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateTokenRequest;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class CreateTokenRequestFundamentalTest extends AbstractFundamentalDtoTest<CreateTokenRequest> {

    private static final int SYMBOL = 2;
    private static final double AMOUNT = 100.0d;
    private static final double GRANULARITY = 1.0d;

    public CreateTokenRequestFundamentalTest() {
        super(CreateTokenRequest::new);
    }

    @Override
    protected void initializeSpecifics(CreateTokenRequest message) {
        message.symbol(SYMBOL);
        message.amount(AMOUNT);
        message.granularity(GRANULARITY);
    }

    @Override
    protected void assertInitializedSpecifics(CreateTokenRequest message) {
        assertEquals(SYMBOL, message.symbol());
        assertEquals(AMOUNT, message.amount(), EPSILON);
        assertEquals(GRANULARITY, message.granularity(), EPSILON);
    }

    @Override
    protected void assertInitializedToString(String s) {
        assertTrue(s.contains("symbol: " + SYMBOL));
        assertTrue(s.contains("amount: " + AMOUNT));
        assertTrue(s.contains("granularity: " + GRANULARITY));
    }

    @Override
    protected Stream<Map.Entry<String, Consumer<CreateTokenRequest>>> forbiddenAfterSign() {
        return Stream.of(
            entry("symbol", m -> m.symbol(1)),
            entry("amount", m -> m.amount(1.0)),
            entry("granularity", m -> m.granularity(1.0))
        );
    }
}
