package net.openhft.chronicle.decentred.dto.fundamental.chainevent;

import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;
import net.openhft.chronicle.decentred.util.DecentredUtil;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public final class TransactionBlockEventFundamentalTest extends AbstractFundamentalDtoTest<TransactionBlockEvent<TransactionBlockEventFundamentalTest.TestMessages>> {

    private static final long CHAIN_ADDRESS = 23424;
    private static final int WEEK_NUMBER = 87;
    private static final long BLOCK_NUMBER = 2322L;

    private static final CreateAddressRequest CREATE_ADDRESS_REQUEST0 = createChild(CreateAddressRequest::new);
    private static final CreateAddressRequest CREATE_ADDRESS_REQUEST1 = createChild(CreateAddressRequest::new);

    public TransactionBlockEventFundamentalTest() {
        super(TransactionBlockEvent::new);
    }

    @Override
    protected void initializeSpecifics(TransactionBlockEvent message) {
        message.chainAddress(CHAIN_ADDRESS);
        message.weekNumber(WEEK_NUMBER);
        message.blockNumber(BLOCK_NUMBER);
        message.addTransaction(CREATE_ADDRESS_REQUEST0);
        message.addTransaction(CREATE_ADDRESS_REQUEST1);
    }

    @Override
    protected void assertInitializedSpecifics(TransactionBlockEvent message) {
        assertEquals(CHAIN_ADDRESS, message.chainAddress());
        assertEquals(WEEK_NUMBER, message.weekNumber(), EPSILON);
        assertEquals(BLOCK_NUMBER, message.blockNumber(), EPSILON);
        assertFalse(message.isEmpty());
    }

    @Override
    protected void assertInitializedToString(String s) {
        System.out.println(s);
        assertTrue(s.contains("chainAddress: " + DecentredUtil.toAddressString(CHAIN_ADDRESS)));
        assertTrue(s.contains("weekNumber: " + WEEK_NUMBER));
        assertTrue(s.contains("blockNumber: " + BLOCK_NUMBER));
        assertTrue(s.contains("createAddressRequest"));
        /*assertTrue(s.contains("\"" + DecentredUtil.toAddressString(ADDRESS0) + "\": " + BLOCK0));
        assertTrue(s.contains("\"" + DecentredUtil.toAddressString(ADDRESS1) + "\": " + BLOCK1));*/
    }

    @Override
    protected Stream<Map.Entry<String, Consumer<TransactionBlockEvent<TestMessages>>>> forbiddenAfterSign() {
        return Stream.of(
            entry("chainAddress", m -> m.chainAddress(1)),
            entry("weekNumber", m -> m.weekNumber(1)),
            entry("blockNumber", m -> m.blockNumber(1))
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
