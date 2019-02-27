package net.openhft.chronicle.decentred.dto.fundamental.address;

import net.openhft.chronicle.decentred.dto.address.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CreateAddressEventFundamentalTest extends AbstractFundamentalDtoTest<CreateAddressEvent> {

    private static final CreateAddressRequest CREATE_ADDRESS_REQUEST = createChild(CreateAddressRequest::new);

    CreateAddressEventFundamentalTest() {
        super(CreateAddressEvent::new);
    }

    @Override
    protected void initializeSpecifics(CreateAddressEvent message) {
        message.createAddressRequest(CREATE_ADDRESS_REQUEST);
    }

    @Override
    protected  void assertInitializedSpecifics(CreateAddressEvent message) {
        assertEquals(CREATE_ADDRESS_REQUEST, message.createAddressRequest());
    }

    @Override
    protected  void assertInitializedToString(String s) {
        assertContains(s, "createAddressRequest");
    }

    @Override
    protected Stream<Map.Entry<String, Consumer<CreateAddressEvent>>> forbiddenAfterSign() {
        return Stream.of(
            entry("createAddressRequest", m -> m.createAddressRequest(CREATE_ADDRESS_REQUEST))
        );
    }
}
