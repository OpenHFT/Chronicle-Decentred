package net.openhft.chronicle.decentred.dto.fundamental.address;

import net.openhft.chronicle.decentred.dto.address.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CreateAddressEventFundamentalTest extends AbstractFundamentalDtoTest<CreateAddressEvent> {

    private final CreateAddressRequest createAddressRequest = createChild(CreateAddressRequest.class);

    CreateAddressEventFundamentalTest() {
        super(CreateAddressEvent.class);
    }

    @Override
    protected void initializeSpecifics(CreateAddressEvent message) {
        message.createAddressRequest(createAddressRequest);
    }

    @Override
    protected  void assertInitializedSpecifics(CreateAddressEvent message) {
        assertEquals(createAddressRequest, message.createAddressRequest());
    }

    @Override
    protected  void assertInitializedToString(String s) {
        assertContains(s, "createAddressRequest");
    }

    @Override
    protected Stream<Map.Entry<String, Consumer<CreateAddressEvent>>> forbiddenAfterSign() {
        return Stream.of(
            entry("createAddressRequest", m -> m.createAddressRequest(createAddressRequest))
        );
    }
}
