package net.openhft.chronicle.decentred.dto.fundamental.chainlifecycle;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.dto.chainlifecycle.AssignDelegatesRequest;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;
import net.openhft.chronicle.decentred.util.KeyPair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public final class AssignDelegatesRequestFundamentalTest extends AbstractFundamentalDtoTest<AssignDelegatesRequest> {

    private static final KeyPair KP0 = new KeyPair(3834323423423L);
    private static final KeyPair KP1 = new KeyPair(-2348723834423L);

    private static final List<BytesStore> DELEGATES = Arrays.asList(
        KP0.publicKey,
        KP1.publicKey
    );

    public AssignDelegatesRequestFundamentalTest() {
        super(AssignDelegatesRequest::new);
    }

    @Override
    protected void initializeSpecifics(AssignDelegatesRequest message) {
        message.delegates(DELEGATES);
    }

    @Override
    protected void assertInitializedSpecifics(AssignDelegatesRequest message) {
        assertEquals(DELEGATES, message.delegates());
    }

    @Override
    protected void assertInitializedToString(String s) {
        assertContains(s, "!!binary hhlyUOyBenQovuHM04/Gj9kfCo0YVvpuUjlPIoJ11rs=");
        assertContains(s, "!!binary T/3gMgGySnXASPfIwAeW0xsNU3Qo3yL24BYs2fHIIPs=");
    }

    @Override
    protected Stream<Map.Entry<String, Consumer<AssignDelegatesRequest>>> forbiddenAfterSign() {
        return Stream.of(
            entry("reason", m -> m.delegates(Collections.emptyList()))
        );
    }
}
