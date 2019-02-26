package net.openhft.chronicle.decentred.dto.fundamental.address;

import net.openhft.chronicle.decentred.dto.address.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.fundamental.AbstractFundamentalDtoTest;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class InvalidationEventFundamentalTest extends AbstractFundamentalDtoTest<InvalidationEvent> {

    public InvalidationEventFundamentalTest() {
        super(InvalidationEvent::new);
    }

    @Override
    protected void initializeSpecifics(InvalidationEvent message) {}

    @Override
    protected void assertInitializedSpecifics(InvalidationEvent message) {}

    @Override
    protected void assertInitializedToString(String s) {}

    @Override
    protected Stream<Map.Entry<String, Consumer<InvalidationEvent>>> forbiddenAfterSign() {
        return Stream.empty();
    }
}
