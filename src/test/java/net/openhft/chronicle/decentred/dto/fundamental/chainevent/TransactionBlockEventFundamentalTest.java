package net.openhft.chronicle.decentred.dto.fundamental.chainevent;

import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


final class TransactionBlockEventFundamentalTest extends AbstractFundamentalDtoTest<TransactionBlockEvent<TransactionBlockEventFundamentalTest.TestMessages>> {

    private static final long CHAIN_ADDRESS = 23424;
    private static final int WEEK_NUMBER = 87;
    private static final long BLOCK_NUMBER = 2322L;

    private static final CreateAddressRequest CREATE_ADDRESS_REQUEST0 = createChild(CreateAddressRequest::new);
    private static final CreateAddressRequest CREATE_ADDRESS_REQUEST1 = createChild(CreateAddressRequest::new);

    TransactionBlockEventFundamentalTest() {
        super(TransactionBlockEvent::new);
    }

    @Override
    protected void initializeSpecifics(TransactionBlockEvent message) {
        message.chainAddress(CHAIN_ADDRESS);
        message.addTransaction(CREATE_ADDRESS_REQUEST0);
        message.addTransaction(CREATE_ADDRESS_REQUEST1);
        message.dtoParser(DtoRegistry.newRegistry(SystemMessages.class).get());
    }

    @Override
    protected void assertInitializedSpecifics(TransactionBlockEvent message) {
        assertEquals(CHAIN_ADDRESS, message.chainAddress());
        assertFalse(message.isEmpty());
    }

    @Override
    protected void assertInitializedToString(String s) {
        System.out.println(s);
        assertContains(s, "chainAddress: " + DecentredUtil.toAddressString(CHAIN_ADDRESS));
        assertContains(s, "transactions:");
        /*assertTrue(s.contains("\"" + DecentredUtil.toAddressString(ADDRESS0) + "\": " + BLOCK0));
        assertTrue(s.contains("\"" + DecentredUtil.toAddressString(ADDRESS1) + "\": " + BLOCK1));*/
    }

    @Override
    protected Stream<Map.Entry<String, Consumer<TransactionBlockEvent<TestMessages>>>> forbiddenAfterSign() {
        return Stream.of(
                entry("chainAddress", m -> m.chainAddress(1))
        );
    }

    // Todo: add specific test (e.g. replay)


    public class TestMessages {

        private AtomicInteger invocationCounter;

        public TestMessages() {
            this.invocationCounter = new AtomicInteger();
        }

        void accept(CreateAddressRequest createAddressRequest) {
            invocationCounter.incrementAndGet();
        }


    };

}
