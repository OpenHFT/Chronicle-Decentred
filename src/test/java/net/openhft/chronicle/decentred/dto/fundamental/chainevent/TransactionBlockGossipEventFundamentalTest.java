package net.openhft.chronicle.decentred.dto.fundamental.chainevent;

import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.LongLongMap;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public final class TransactionBlockGossipEventFundamentalTest extends AbstractFundamentalDtoTest<TransactionBlockGossipEvent> {

    private static final long CHAIN_ADDRESS = 23424;

    private static final long ADDRESS0 = 2325353244323L;
    private static final long ADDRESS1 = ADDRESS0 + (1L << 32);

    private static final long BLOCK0 = 927634241L;
    private static final long BLOCK1 = 294572682L;

    public TransactionBlockGossipEventFundamentalTest() {
        super(TransactionBlockGossipEvent::new);
    }

    @Override
    protected void initializeSpecifics(TransactionBlockGossipEvent message) {
        message.chainAddress(CHAIN_ADDRESS);
        final LongLongMap map = message.addressToBlockNumberMap();
        map.justPut(ADDRESS0, BLOCK0);
        map.justPut(ADDRESS1, BLOCK1);
    }

    @Override
    protected void assertInitializedSpecifics(TransactionBlockGossipEvent message) {
        assertEquals(CHAIN_ADDRESS, message.chainAddress());
    }

    @Override
    protected void assertInitializedToString(String s) {
        System.out.println(s);
        assertTrue(s.contains("chainAddress: " + DecentredUtil.toAddressString(CHAIN_ADDRESS)));
        assertTrue(s.contains("addressToBlockNumberMap"));
        assertTrue(s.contains("\"" + DecentredUtil.toAddressString(ADDRESS0) + "\": " + BLOCK0));
        assertTrue(s.contains("\"" + DecentredUtil.toAddressString(ADDRESS1) + "\": " + BLOCK1));
    }

    @Override
    protected Stream<Map.Entry<String, Consumer<TransactionBlockGossipEvent>>> forbiddenAfterSign() {
        return Stream.of(
                entry("chainAddress", m -> m.chainAddress(1))
            );
    }

}
