package net.openhft.chronicle.decentred.dto.fundamental.base;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.api.AddressManagementRequests;
import net.openhft.chronicle.decentred.api.ConnectionStatusListener;
import net.openhft.chronicle.decentred.api.SystemMessageListener;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.base.TransientFieldHandler;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.dto.base.trait.HasDtoParser;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.MicroTimestampLongConverter;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fundamental properties of a single VanillaSignedMessage.
 *
 * @param <T> message type
 */
public abstract class AbstractFundamentalDtoTest<T extends VanillaSignedMessage<T>> {

    protected static final double EPSILON = 1e-7;

    private final long SEED = 1;
    protected final KeyPair KEY_PAIR = new KeyPair(SEED);

    private static final long DEFAULT_LONG = 42L;
    protected static final long DEFAULT_ADDRESS = 96L;
    private static final int DEFAULT_PROTOCOL = 17;
    private static final int DEFAULT_MESSAGE_TYPE = 0xFFF2;
    private final Consumer<T> NO_OP = m -> {};

    protected final DtoRegistry<SystemMessages> registry;
    protected final Supplier<T> constructor;
    protected final long timeMS;

    protected T instance;

    protected AbstractFundamentalDtoTest(@NotNull Class<? super T> clazz) {
        registry = DtoRegistry.newRegistry(SystemMessages.class)
            .addProtocol(1, SystemMessageListener.class)
            .addProtocol(2, AddressManagementRequests.class)
            .addProtocol(3, ConnectionStatusListener.class);

        @SuppressWarnings("unchecked")
        final Class<T> castedClass = (Class<T>)clazz;

        this.constructor = () -> registry.create(castedClass);
        this.timeMS = UniqueMicroTimeProvider.INSTANCE.currentTimeMicros();
    }

    public static void assertContains(String original, String find) {
        assertTrue(original.contains(find), "\"" + original + "\" doesn't contain \"" + find + "\"");
    }

    protected final void initialize(T message) {
        message.timestampUS(timeMS);
        message.address(DecentredUtil.toAddress(KEY_PAIR.publicKey));
        ensureDtoParserSetIfHasDtoParser(message);
        initializeSpecifics(message);
    }

    protected abstract void initializeSpecifics(T message);

    protected abstract void assertInitializedSpecifics(T message);

    protected abstract void assertInitializedToString(String s);

    /**
     * Returns a new Stream of paired name and operations that is forbidden after
     * the message has been signed.
     *
     * @return a new Stream of paired name and operations that is forbidden after
     * the message has been signed
     */
    protected abstract Stream<Map.Entry<String, Consumer<T>>> forbiddenAfterSign();

    @BeforeEach
    void setup() {
        instance = constructor.get();
    }

    @Test
    void testConstructor() {
        assertNotNull(instance);
    }

    @Test
    void testInitialized() {
        initialize(instance);
        assertTrue(instance.protocol() != 0);
        assertTrue(instance.messageType() != 0);
        assertEquals(timeMS, instance.timestampUS());
        assertEquals(DecentredUtil.toAddress(KEY_PAIR.publicKey), instance.address());
        assertInitializedSpecifics(instance);
    }

    @Test
    void testInitialAddress() {
        assertEquals(0L, instance.address());
    }

    @Test
    public void testAddress() {
        instance.address(DEFAULT_ADDRESS);
        assertEquals(DEFAULT_ADDRESS, instance.address());
    }

    @Test
    void testIntialTimestampUS() {
        assertEquals(0, instance.timestampUS());
    }

    @Test
    void testTimestampUS() {
        instance.timestampUS(DEFAULT_LONG);
        assertEquals(DEFAULT_LONG, instance.timestampUS());
    }

    @Test
    void testInitialThrowers() {
        assertThrows(Throwable.class, () -> {
            System.out.println(instance.toHexString());
        });
    }

    @Test
    void testInitialProtocol() {
        assertEquals(registry.protocolFor(instance.getClass()), instance.protocol());
    }

    @Test
    void testProtocol() {
        instance.protocol(DEFAULT_PROTOCOL);
        assertEquals(DEFAULT_PROTOCOL, instance.protocol());
    }

    @Test
    void testInitialMessageType() {
        assertEquals(registry.messageTypeFor(instance.getClass()), instance.messageType());
    }

    @Test
    void testMessageType() {
        instance.messageType(DEFAULT_MESSAGE_TYPE);
        assertEquals(DEFAULT_MESSAGE_TYPE, instance.messageType());
    }

    @Test
    void testInitialSign() {
        assertFalse(instance.signed());
    }

@Test
    void testToHexString() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        final String s = instance.toHexString();
        assertContains(s, "timestampUS" );
        assertContains(s, "address" );
    }

    @Test
    void testToString() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);

        final StringBuilder sb = new StringBuilder();
        new MicroTimestampLongConverter().append(sb, instance.timestampUS());
        final String expectedTimeString = sb.toString();

        final String s = instance.toString();
        // System.out.println(s);
        assertContains(s, "timestampUS: " + expectedTimeString);
        assertContains(s, "address: " + DecentredUtil.toAddressString(instance.address()));
        assertInitializedToString(s);
    }

    @Test
    void testEqualsUninitialized() {
        testEquals(NO_OP);
    }

    @Test
    void testEquals() {
        testEquals(this::initialize);
    }

    @Test
    void testHashCodeUninitialized() {
        testHashcode(NO_OP);
    }

    @Test
    void testHashCode() {
        testHashcode(this::initialize);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 147, 63, 64, 65 }) // Cover the case of mid Byte serialization
    protected void testMarshallUnMarshallBytes(int offset) {
        final Bytes bytes = Bytes.allocateElasticDirect(1000);
        bytes.writePosition(offset);

        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        instance.writeMarshallable(bytes);
        final long writePosition = bytes.writePosition();

        final T actual = constructor.get();
        ensureDtoParserSetIfHasDtoParser(actual);

        bytes.readPosition(offset);
        actual.readMarshallable(bytes);

        String s = actual.toString();

        //Todo: Make sure that all bytes are consumed. How? readMarshallable does not modify bytes

        assertEqualsDoubleSided(instance, actual);
    }

    @Test
    void testVerify() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        assertTrue(instance.verify(a -> KEY_PAIR.publicKey));
    }

    @Test
    void testForbiddenOperators() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        final List<String> failed = new ArrayList<>();
        forbiddenAfterSign().forEach(e -> {
            try {
                e.getValue().accept(instance);
                failed.add(e.getKey());
                //fail("The operation " + e.getKey() + " was not disallowed.");
            } catch (Throwable ignored) {
                // ignore
            }
        });
        if (!failed.isEmpty()) {
            fail("These operations " + failed + " was not disallowed.");
        }
    }

    @Test
    void testPublicKey() {
        final boolean hasPublicKey = instance.hasPublicKey();
        final BytesStore b = instance.publicKey();
        if (hasPublicKey) {
            instance.publicKey(KEY_PAIR.publicKey);
            assertEquals(KEY_PAIR.publicKey, instance.publicKey());
        } else {
            assertTrue(b.isEmpty());
        }
    }

    @Test
    void testReset() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        instance.reset();
        assertFalse(instance.signed());

        final T other = constructor.get();
        assertEquals(other, instance);
    }

    @Test
    void testByteBufferUnsigned() {
        assertThrows(IllegalStateException.class, () -> {
            instance.byteBuffer();
        });
    }

    @Test
    void testByteBuffer() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        final ByteBuffer bb = instance.byteBuffer();
        assertEquals(0, bb.position());
        assertTrue(bb.capacity() > 0);
    }

/// AbstractBytesMarshallable

    @Test
    @Disabled("TODO FIX")
    public void testMarshallUnMarshallWire() {
        final Wire wire = new TextWire(Bytes.allocateElasticDirect(1000));
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        instance.writeMarshallable(wire);
        System.out.println(wire);
        final T actual = constructor.get();
        ensureDtoParserSetIfHasDtoParser(actual);
        actual.readMarshallable(wire);

        assertEqualsDoubleSided(instance, actual);
    }

/// Marshallable

    @Test
    void testDeepCopy() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        final T copy = instance.deepCopy();
        assertEqualsDoubleSided(instance, copy);
    }

    @Test
    void testCopyTo() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        final T copy = constructor.get();
        instance.copyTo(copy);
        assertEqualsDoubleSided(instance, copy);
    }

    @Test
    void testCopyToWithIllegalTargetClass() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        final VanillaSignedMessage copy = new VanillaSignedMessage() {
            @Override
            public TransientFieldHandler transientFieldHandler() {
                return TransientFieldHandler.empty();
            }
        };
        assertThrows(IllegalArgumentException.class, () -> {
            instance.copyTo(copy);
        });
    }

    @Test
    void testCopyToWithNull() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        final Marshallable m = null;
        assertThrows(Exception.class, () -> {
            instance.copyTo(m);
        });
    }

    protected Map.Entry<String, Consumer<T>> entry(String s, Consumer<T> c) {
        return new AbstractMap.SimpleImmutableEntry<>(s, c);
    }

    protected <C extends VanillaSignedMessage<C>> C createChild(Class<C> clazz) {
        return createChild(clazz, m -> {}, new Random(42).nextLong());
    }

    protected <C extends VanillaSignedMessage<C>> C createChild(Class<C> clazz, long seed) {
        return createChild(clazz, m -> {}, seed);
    }

    protected <C extends VanillaSignedMessage<C>> C createChild(Class<C> clazz, Consumer<C> initializer, long seed) {
        final KeyPair kp = new KeyPair(seed);
        final C result = registry.create(clazz)
            .address(DecentredUtil.toAddress(kp.publicKey));
            ensureDtoParserSetIfHasDtoParser(result);
            initializer.accept(result);
            return result.sign(kp.secretKey);
    }

    protected <M extends VanillaSignedMessage<M>> void ensureDtoParserSetIfHasDtoParser(M message) {
        if (message instanceof HasDtoParser) {
            final HasDtoParser hasDtoParser = ((HasDtoParser) message);
            hasDtoParser.dtoParser(registry.get());
        }
    }

    protected void assertEqualsDoubleSided(T expected, T actual) {
        assertEquals(expected, actual);
        assertEquals(actual, expected);
    }

    private void testEquals(Consumer<T> initializer) {
        final T other = constructor.get();
        initializer.accept(instance);
        initializer.accept(other);
        assertEquals(instance, other);
        assertEquals(other, instance);
    }

    private void testHashcode(Consumer<T> initializer) {
        final T other = constructor.get();
        initializer.accept(instance);
        initializer.accept(other);
        assertEquals(instance, other);
        assertEquals(other, instance);
    }

/*    private <R> void assertThrowsBeforeSign(Function<T, R> mapper) {
        try {
            final R actual = mapper.apply(instance);
            fail("Calling this method before sign() is called should produce an Exception");
        } catch (Throwable ignored) {
            // Do nothing
        }
    }*/

}
