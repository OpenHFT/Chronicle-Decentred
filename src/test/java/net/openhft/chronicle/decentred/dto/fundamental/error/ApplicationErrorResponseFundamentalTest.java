package net.openhft.chronicle.decentred.dto.fundamental.error;

import net.openhft.chronicle.decentred.dto.error.ApplicationErrorResponse;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class ApplicationErrorResponseFundamentalTest extends AbstractFundamentalDtoTest<ApplicationErrorResponse> {

    private static final String REASON = "CreateAddressRequest failed due to some reason";
    private static final CreateAddressRequest ORIGINAL_MESSAGE = createChild(CreateAddressRequest::new);

    public ApplicationErrorResponseFundamentalTest() {
        super(ApplicationErrorResponse::new);
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
        assertTrue(s.contains(REASON));

    }

    @Override
    protected Stream<Map.Entry<String, Consumer<ApplicationErrorResponse>>> forbiddenAfterSign() {
        return Stream.of(
            entry("reason", m -> m.reason("A"))
        );
    }
}
