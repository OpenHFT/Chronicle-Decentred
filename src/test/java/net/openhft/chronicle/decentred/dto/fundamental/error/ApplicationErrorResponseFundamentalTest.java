package net.openhft.chronicle.decentred.dto.fundamental.error;

import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.error.ApplicationErrorResponse;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;
import net.openhft.chronicle.decentred.util.DtoRegistry;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ApplicationErrorResponseFundamentalTest extends AbstractFundamentalDtoTest<ApplicationErrorResponse> {

    private static final String REASON = "CreateAddressRequest failed due to some reason";
    private final CreateAddressRequest ORIGINAL_MESSAGE = createChild(CreateAddressRequest::new);

    ApplicationErrorResponseFundamentalTest() {
        super(ApplicationErrorResponseFundamentalTest::create);

    }

    private static ApplicationErrorResponse create() {
        return new ApplicationErrorResponse()
                .dtoParser(DtoRegistry.newRegistry(SystemMessages.class).get());
    }

    @Override
    protected void initializeSpecifics(ApplicationErrorResponse message) {
        message.init(ORIGINAL_MESSAGE, REASON);
    }

    @Override
    protected void assertInitializedSpecifics(ApplicationErrorResponse message) {
        assertEquals(REASON, message.reason());
        assertEquals(ORIGINAL_MESSAGE, message.origMessage());
    }

    @Override
    protected void assertInitializedToString(String s) {
        assertContains(s, REASON);
    }

    @Override
    protected Stream<Map.Entry<String, Consumer<ApplicationErrorResponse>>> forbiddenAfterSign() {
        return Stream.of(
            entry("reason", m -> m.reason("A"))
        );
    }
}
