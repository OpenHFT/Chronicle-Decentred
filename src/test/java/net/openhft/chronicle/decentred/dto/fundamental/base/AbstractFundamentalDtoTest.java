package net.openhft.chronicle.decentred.dto.fundamental.base;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.MicroTimestampLongConverter;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.*;

/**
 * Tests fundamental properties of a single VanillaSignedMessage.
 *
 * @param <T>
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

    public static void assertContains(String s, String find) {
/*
        assertTrue(s.contains(find));
    }
    public static void assertContains2(String s, String find) {
*/
        assertTrue("\"" + s + "\" doesn't contain \"" + find + "\"",
                s.contains(find));
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

    @Before
    public void setup() {
        instance = constructor.get();
    }

    @Test
    public void testConstructor() {
        assertNotNull(instance);
    }

    @Test
    public void testInitialized() {
        initialize(instance);
        assertEquals(DEFAULT_PROTOCOL, instance.protocol());
        assertEquals(DEFAULT_MESSAGE_TYPE, instance.messageType());
        assertEquals(timeMS, instance.timestampUS());
        assertEquals(DecentredUtil.toAddress(KEY_PAIR.publicKey), instance.address());
        assertInitializedSpecifics(instance);
    }

    @Test
    public void testInitialAddress() {
        assertEquals(0L, instance.address());
    }

    public void testAddress() {
        instance.address(DEFAULT_LONG);
        assertEquals(DEFAULT_LONG, instance.address());
    }

    @Test
    public void testIntialTimestampUS() {
        assertEquals(0L, instance.address());
    }

    @Test
    public void testTimestampUS() {
        instance.timestampUS(DEFAULT_LONG);
        assertEquals(DEFAULT_LONG, instance.timestampUS());
    }

/*    @Test
    public void testIntialVerify() {
        assertFalse(instance.verify(a -> KEY_PAIR.publicKey));
    }*/

    @Test
    public void testInitialThrowers() {
        assertThrowsBeforeSign(VanillaSignedMessage::toHexString);
    }

    @Test
    public void testInitialProtocol() {
        assertEquals(0L, instance.protocol());
    }

    @Test
    public void testProtocol() {
        instance.protocol(DEFAULT_PROTOCOL);
        assertEquals(DEFAULT_PROTOCOL, instance.protocol());
    }

    @Test
    public void testInitialMessageType() {
        assertEquals(0L, instance.messageType());
    }

    @Test
    public void testMessageType() {
        instance.messageType(DEFAULT_MESSAGE_TYPE);
        assertEquals(DEFAULT_MESSAGE_TYPE, instance.messageType());
    }

    @Test
    public void testInitialSign() {
        assertFalse(instance.signed());
    }

    @Test
    public void testToString() {
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
    public void testEqualsUninitialized() {
        testEquals(NO_OP);
    }

    @Test
    public void testEquals() {
        testEquals(this::initialize);
    }

    @Test
    public void testHashCodeUninitialized() {
        testHashcode(NO_OP);
    }

    @Test
    public void testHashCode() {
        testHashcode(this::initialize);
    }

    @Test
    public void testMarshallUnMarshallBytes() {
        final Bytes bytes = Bytes.allocateElasticDirect(1000);
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        instance.writeMarshallable(bytes);

        final T actual = constructor.get();
        bytes.readPosition(0);
        actual.readMarshallable(bytes);

        assertEquals(instance, actual);
        assertEquals(actual, instance);
    }

    @Test
    public void testMarshallUnMarshallWire() {
        final Wire wire = new TextWire(Bytes.allocateElasticDirect(1000));
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        instance.writeMarshallable(wire);
        System.out.println(wire);
        final T actual = constructor.get();
        actual.readMarshallable(wire);

        assertEquals(instance, actual);
        assertEquals(actual, instance);
    }

    @Test
    public void testVerify() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);

        assertTrue(instance.verify(a -> KEY_PAIR.publicKey));
    }


    @Test
    public void testForbiddenOperators() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        forbiddenAfterSign().forEach(e -> {
            try {
                e.getValue().accept(instance);
                fail("The operation " + e.getKey() + " was not disallowed.");
            } catch (AssertionError ignored) {
                // ignore
            }
        });
    }

    @Test
    public void testPublicKey() {
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
    public void testReset() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        instance.reset();
        assertFalse(instance.signed());

        final T other = constructor.get();
        assertEquals(other, instance);
    }

    @Test
    public void testByteBufferUnsigned() {
        try {
            instance.byteBuffer();
            fail("unsigned message was not captured");
        } catch (Error ignore) {
            // ignore
        }
    }

    @Test
    public void testByteBuffer() {
        initialize(instance);
        instance.sign(KEY_PAIR.secretKey);
        final ByteBuffer bb = instance.byteBuffer();
        assertEquals(0, bb.position());
        assertTrue(bb.capacity() > 0);
    }


    // Todo: copyTo

    // Todo: Other super methods like copyTo

    // Check Bytes after serializer


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

    private <R> void assertThrowsBeforeSign(Function<T, R> mapper) {
        try {
            final R actual = mapper.apply(instance);
            fail("Calling this method before sign() is called should produce an Exception");
        } catch (Exception ignored) {
            // Do nothing
        }
    }



}
