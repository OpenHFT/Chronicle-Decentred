package net.openhft.chronicle.decentred.dto.fundamental.base;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.base.TransientFieldHandler;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.MicroTimestampLongConverter;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fundamental properties of a single VanillaSignedMessage.
 *
 * @param <T> message type
 */
public abstract class AbstractFundamentalDtoTest<T extends VanillaSignedMessage<T>> {

    protected static final double EPSILON = 1e-7;

    private final long SEED = 1;
    private final KeyPair KEY_PAIR = new KeyPair(SEED);

    private static final long DEFAULT_LONG = 42L;
    private static final int DEFAULT_PROTOCOL = 17;
    private static final int DEFAULT_MESSAGE_TYPE = 0xFFF2;
    private final Consumer<T> NO_OP = m -> {};

    private final Supplier<T> constructor;
    private final long timeMS;

    private T instance;

    protected AbstractFundamentalDtoTest(Supplier<T> constructor) {
        this.constructor = requireNonNull(constructor);
        this.timeMS = UniqueMicroTimeProvider.INSTANCE.currentTimeMicros();
    }

    public static void assertContains(String original, String find) {
        assertTrue(original.contains(find), "\"" + original + "\" doesn't contain \"" + find + "\"");
    }

    final void initialize(T message) {
        message.protocol(DEFAULT_PROTOCOL);
        message.messageType(DEFAULT_MESSAGE_TYPE);
        message.timestampUS(timeMS);
        message.address(DecentredUtil.toAddress(KEY_PAIR.publicKey));
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
        assertEquals(DEFAULT_PROTOCOL, instance.protocol());
        assertEquals(DEFAULT_MESSAGE_TYPE, instance.messageType());
        assertEquals(timeMS, instance.timestampUS());
        assertEquals(DecentredUtil.toAddress(KEY_PAIR.publicKey), instance.address());
        assertInitializedSpecifics(instance);
    }

    @Test
    void testInitialAddress() {
        assertEquals(0L, instance.address());
    }

    public void testAddress() {
        instance.address(DEFAULT_LONG);
        assertEquals(DEFAULT_LONG, instance.address());
    }

    @Test
    void testIntialTimestampUS() {
        assertEquals(0L, instance.address());
    }

    @Test
    void testTimestampUS() {
        instance.timestampUS(DEFAULT_LONG);
        assertEquals(DEFAULT_LONG, instance.timestampUS());
    }

    @Test
    void testInitialThrowers() {
        assertThrows(Throwable.class, () -> {
            instance.toHexString();
        });
    }

    @Test
    void testInitialProtocol() {
        assertEquals(0L, instance.protocol());
    }

    @Test
    void testProtocol() {
        instance.protocol(DEFAULT_PROTOCOL);
        assertEquals(DEFAULT_PROTOCOL, instance.protocol());
    }

    @Test
    void testInitialMessageType() {
        assertEquals(0L, instance.messageType());
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
        final StringBuilder sb = new StringBuilder();
        new MicroTimestampLongConverter().append(sb, timeMS);
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
    void testMarshallUnMarshallBytes(int offset) {
        final Bytes bytes = Bytes.allocateElasticDirect(1000);
        bytes.writePosition(offset);
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        instance.writeMarshallable(bytes);
        final long writePosition = bytes.writePosition();

        final T actual = constructor.get();
        bytes.readPosition(offset);
        actual.readMarshallable(bytes);

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
    void testMarshallUnMarshallWire() {
        final Wire wire = new TextWire(Bytes.allocateElasticDirect(1000));
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        instance.writeMarshallable(wire);
        System.out.println(wire);
        final T actual = constructor.get();
        actual.readMarshallable(wire);

        assertEqualsDoubleSided(instance, actual);
    }


    /// Marshallable

    @Disabled
    @Test
    void testDeepCopy() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        final T copy = instance.deepCopy();
        assertEqualsDoubleSided(instance, copy);
    }

    @Disabled
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

    protected static <C extends VanillaSignedMessage<C>> C createChild(Supplier<C> constructor) {
        return createChild(constructor, m -> {});
    }


    protected static <C extends VanillaSignedMessage<C>> C createChild(Supplier<C> constructor, Consumer<C> initializer) {
        final C result = constructor.get()
            .protocol(1)
            .messageType(1)
            .address(3);
            initializer.accept(result);
            return result.sign(new KeyPair(933448745).secretKey);
    }

    private void assertEqualsDoubleSided(T expected, T actual) {
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
