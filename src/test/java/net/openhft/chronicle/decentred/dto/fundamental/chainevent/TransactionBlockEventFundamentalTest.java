package net.openhft.chronicle.decentred.dto.fundamental.chainevent;

import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.address.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.chainevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.dto.chainlifecycle.AssignDelegatesRequest;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateChainRequest;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateTokenRequest;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.KeyPair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

final class TransactionBlockEventFundamentalTest extends AbstractFundamentalDtoTest<TransactionBlockEvent<SystemMessages>> {

    private static final long CHAIN_ADDRESS = 23424;
    private static final long ADDRESS0_SEED = 12635615L;
    private static final long ADDRESS1_SEED = ADDRESS0_SEED + 1;


    private final CreateAddressRequest createAddressRequest0 = createChild(CreateAddressRequest.class, ADDRESS0_SEED);
    private final CreateAddressRequest createAddressRequest1 = createChild(CreateAddressRequest.class, ADDRESS1_SEED);

    TransactionBlockEventFundamentalTest() {
        super(TransactionBlockEvent.class);
    }

    @Override
    protected void initializeSpecifics(TransactionBlockEvent message) {
        message.chainAddress(CHAIN_ADDRESS);
        message.addTransaction(createAddressRequest0);
        message.addTransaction(createAddressRequest1);
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
        assertContains(s, DecentredUtil.toAddressString(createAddressRequest0.address()));
        assertContains(s, DecentredUtil.toAddressString(createAddressRequest1.address()));
    }

    @Override
    protected Stream<Map.Entry<String, Consumer<TransactionBlockEvent<SystemMessages>>>> forbiddenAfterSign() {
        return Stream.of(
            entry("chainAddress", m -> m.chainAddress(1))
        );
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 8}) // Make sure we can replay multiple times
    void replay(int iterations) {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);

        for (int i = 0; i < iterations; i++) {
            final Tester tester = new Tester();
            instance.replay(tester);
            assertEquals(2, tester.count(CreateAddressRequest.class));

            final CreateAddressRequest r0 = tester.createAddressRequestList().get(0);
            final CreateAddressRequest r1 = tester.createAddressRequestList().get(1);

            assertEquals(createAddressRequest0, r0);
            assertEquals(createAddressRequest1, r1);
        }
    }

    private final class Tester implements SystemMessages {

        private final Map<String, AtomicInteger> invocationCountMap = new ConcurrentHashMap<>();
        private final List<CreateAddressRequest> createAddressRequestList = new ArrayList<>();

        @Override
        public void createChainRequest(CreateChainRequest createChainRequest) {

        }

        @Override
        public void assignDelegatesRequest(AssignDelegatesRequest assignDelegatesRequest) {

        }

        @Override
        public void createTokenRequest(CreateTokenRequest createTokenRequest) {

        }

        @Override
        public void createAddressRequest(CreateAddressRequest createAddressRequest) {
            add(createAddressRequest);
            System.out.println("Tester got: " + createAddressRequest.toString());
            System.out.println(System.identityHashCode(createAddressRequest));

            CreateAddressRequest copy = registry.create(CreateAddressRequest.class);
            // Messages are reused!!!!
            createAddressRequest.copyTo(copy);

            createAddressRequestList.add(copy);
        }

        @Override
        public void verificationEvent(VerificationEvent verificationEvent) {

        }

        @Override
        public void invalidationEvent(InvalidationEvent invalidationEvent) {

        }

        @Override
        public void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent) {

        }

        @Override
        public void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent) {

        }

        @Override
        public void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent) {

        }

        @Override
        public void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent) {

        }

        private <T extends SignedMessage> void add(T msg) {
            add(msg.getClass());
        }

        private <T extends SignedMessage> void add(Class<T> clazz) {
            add(clazz.getSimpleName());
        }

        private void add(String key) {
            invocationCountMap.computeIfAbsent(key, $ -> new AtomicInteger()).incrementAndGet();
        }

        private <T extends SignedMessage> int count(Class<T> clazz) {
            return count(clazz.getSimpleName());
        }

        public int count(String key) {
            return invocationCountMap.getOrDefault(key, new AtomicInteger()).get();
        }

        public List<CreateAddressRequest> createAddressRequestList() {
            return Collections.unmodifiableList(createAddressRequestList);
        }


    }

}