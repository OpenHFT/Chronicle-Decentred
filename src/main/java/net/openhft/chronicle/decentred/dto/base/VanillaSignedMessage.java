package net.openhft.chronicle.decentred.dto.base;

import net.openhft.chronicle.bytes.*;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.base.trait.HasTransientFieldHandler;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.decentred.util.ShortUtil;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;

public abstract class VanillaSignedMessage<T extends VanillaSignedMessage<T>> extends AbstractBytesMarshallable
    implements SignedMessage, HasTransientFieldHandler<T> {

    private static final int LENGTH = 0;
    private static final int LENGTH_END = LENGTH + Integer.BYTES;
    private static final int SIGNATURE = LENGTH_END;
    private static final int SIGNATURE_END = SIGNATURE + Ed25519.SIGNATURE_LENGTH;
    public static final int MESSAGE_TYPE = SIGNATURE_END;
    private static final int MESSAGE_TYPE_END = MESSAGE_TYPE + Short.BYTES;
    private static final int PROTOCOL = MESSAGE_TYPE_END;
    private static final int PROTOCOL_END = PROTOCOL + Short.BYTES;
    private static final int MESSAGE_START = PROTOCOL_END;

    private static final Field BB_ADDRESS = Jvm.getField(ByteBuffer.allocateDirect(0).getClass(), "address");
    private static final Field BB_CAPACITY = Jvm.getField(ByteBuffer.allocateDirect(0).getClass(), "capacity");

    private static final Set<String> BASE_TRANSIENT_FIELD_NAMES =
        Stream.of(VanillaSignedMessage.class.getDeclaredFields())
            .filter(f -> Modifier.isTransient(f.getModifiers()))
            .map(Object::toString)
            .collect(toSet());

    private static final boolean ENFORCE_TRANSIENT_OVERRIDE_INVARIANT = true;


    // for writing to a new set of bytes
    private transient Bytes tempBytes = Bytes.allocateElasticDirect(4L << 10);
    // for reading an existing Bytes
    private transient PointerBytesStore readPointer = BytesStore.nativePointer();
    protected transient Bytes<Void> bytes = readPointer.bytesForRead();

    private transient boolean signed = false;
    private transient ByteBuffer byteBuffer;
    // unsigned 16-bit
    private transient int messageType, protocol;
    @LongConversion(MicroTimestampLongConverter.class)
    private long timestampUS;
    @LongConversion(AddressLongConverter.class)
    private long address;


    public VanillaSignedMessage() {

/*        Set<String> objectM = Stream.of(Object.class.getMethods())
            .map(Object::toString)
            .collect(Collectors.toSet());

        System.out.println( Stream.of(getClass().getMethods())
            .sorted(comparing(m -> m .getDeclaringClass().getName()))
            .map(Object::toString)
            .filter(m -> !objectM.contains(m))
            .collect(Collectors.joining("\n")));
        System.exit(1);*/


        if (ENFORCE_TRANSIENT_OVERRIDE_INVARIANT) {
            final Set<String> newTransientFields = Stream.of(getClass().getDeclaredFields())
                .filter(f -> Modifier.isTransient(f.getModifiers()))
                .map(Object::toString)
                .filter(n -> !BASE_TRANSIENT_FIELD_NAMES.contains(n))
                .collect(toSet());

            if (!newTransientFields.isEmpty() && transientFieldHandler() == TransientFieldHandler.empty()) {
                throw new IllegalStateException("The class " + getClass().getName() + " declares transitive field(s) " + newTransientFields + " but does not override transientFieldHandler()");
            }
        }
    }

    @Override
    public TransientFieldHandler<T> transientFieldHandler() {
        return TransientFieldHandler.empty();
    }

    @Override
    public final void writeMarshallable(@NotNull WireOut wire) {
        super.writeMarshallable(wire);
        transientFieldHandler().writeMarshallable(self(), wire);
    }

    @Override
    public final void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        // reset() ????
        signed = false;
        super.readMarshallable(wire);
        transientFieldHandler().readMarshallable(self(), wire);
    }

    @Override
    public final void readMarshallable(BytesIn bytes) throws IORuntimeException {
        long capacity = bytes.readRemaining();
        readPointer.set(bytes.addressForRead(bytes.readPosition()), capacity);
        messageType = readPointer.readUnsignedShort(MESSAGE_TYPE);
        protocol = readPointer.readUnsignedShort(PROTOCOL);

        this.bytes.clear();
        this.bytes.readPositionRemaining(MESSAGE_START, capacity - MESSAGE_START);
        super.readMarshallable(this.bytes);
        signed = true;
        transientFieldHandler().readMarshallable(self(), this.bytes);
    }

    /**
     * Resets all properties of this message to their default values. This sets
     * the message in the same state as if it was just created.
     */
    public final void reset() {
        signed = false;
        messageType = 0;
        protocol = 0;
        // address = timestampUS = 0; set by super.reset();
        super.reset();
        transientFieldHandler().reset(self());
    }

    @Override
    public final boolean signed() {
        return signed;
    }

    /**
     * Writes the content of this signed message to the provided bytes. The source
     * of the content is taken from an internal Bytes store.
     *
     * @param bytes to write to
     */
    @Override
    public final void writeMarshallable(BytesOut bytes) {
        assertSigned();
        bytes.write(this.bytes, 0, this.bytes.readLimit());
    }

    /**
     * Writes the content of the fields of this messages (non-transitive and non-transitive)
     * fields to the proved bytes.
     * <P>
     *  This method is called upon signing this message.
     *
     * @param bytes to write to
     */
    private void writeMarshallableInternal(BytesOut bytes) {
        super.writeMarshallable(bytes);
        transientFieldHandler().writeMarshallableInternal(self(), bytes);
    }

    @Override
    public final long address() {
        return address;
    }

    /**
     * Sets the address of this message.
     *
     * @param address to use
     * @return this message.
     *
     * @throws IllegalStateException if the message was already signed
     */
    public final T address(long address) {
        assertNotSigned();
        this.address = address;
        return self();
    }

    @Override
    public final long timestampUS() {
        return timestampUS;
    }

    /**
     * Sets the timestamp in ms of this message.
     *
     * @param timestampUS to use
     * @return this message.
     *
     * @throws IllegalStateException if the message was already signed
     */
    public final T timestampUS(long timestampUS) {
        assertNotSigned();
        this.timestampUS = timestampUS;
        return self();
    }

     // Signifies this message contains it's own public key.
    @Override
    public BytesStore publicKey() {
        return NoBytesStore.noBytesStore();
    }

    public boolean hasPublicKey() {
        return false;
    }

    public T publicKey(@NotNull BytesStore key) {
        throw new UnsupportedOperationException("This method is not supported for this message type. Only for " + SelfSignedMessage.class.getSimpleName());
    }

    @Override
    public final T sign(@NotNull BytesStore secretKey) {
        UniqueMicroTimeProvider timeProvider = UniqueMicroTimeProvider.INSTANCE;
        return sign(secretKey, timeProvider);
    }

    /**
     * Signs this message with the provided {@code secretKey} and provided
     * {@code timeProvider}.
     * <p>
     * After a message has been signed, its properties cannot be changed any more.
     *
     * @param secretKey to use for signing
     * @param timeProvider to use for generating a timestamp
     * @return this message

     * @throws IllegalStateException if this message has already been signed
     * or if the protocol has not been set or if the message type has
     * not been set.
     *
     */
    public final T sign(@NotNull BytesStore secretKey, @NotNull TimeProvider timeProvider) {
        assertNotSigned();
        if (protocol == 0) {
            throw new IllegalStateException("The protocol must be set before signing");
        }
        if (messageType == 0) {
            throw new IllegalStateException("The message type must be set before signing");
        }

        if (hasPublicKey())
            publicKey(secretKey);

        address = secretKey.readLong(secretKey.readRemaining() - Long.BYTES);
        timestampUS = timeProvider.currentTimeMicros();

        tempBytes.clear();
        tempBytes.writeInt(0); // Provisional length
        long signatureStart = tempBytes.writePosition();
        tempBytes.writeSkip(Ed25519.SIGNATURE_LENGTH);
        tempBytes.writeUnsignedShort(messageType);
        tempBytes.writeUnsignedShort(protocol);
        writeMarshallableInternal(tempBytes);
        long length = tempBytes.readRemaining();
        tempBytes.writeUnsignedInt(LENGTH, length);
        tempBytes.readPosition(signatureStart);
        Ed25519.sign(tempBytes, secretKey);
        signed = true;
        readPointer.set(tempBytes.addressForRead(0), length);
        bytes.writeLimit(length)
                .readPositionRemaining(0, length);
        return self();
    }

    public final String toHexString() {
        HexDumpBytes dump = new HexDumpBytes()
                .offsetFormat((o, b) -> b.appendBase16(o, 4));
        dump.comment("length").writeUnsignedInt(bytes.readUnsignedInt(LENGTH));
        dump.comment("signature start").write(bytes, (long) SIGNATURE, Ed25519.SIGNATURE_LENGTH);
        dump.comment("signature end");
        dump.comment("messageType").writeUnsignedShort(messageType);
        dump.comment("protocol").writeUnsignedShort(protocol);
        writeMarshallableInternal(dump);
        String text = dump.toHexString();
        dump.release();
        return text;
    }

    public final boolean verify(LongFunction<BytesStore> addressToPublicKey) {
        BytesStore publicKey = hasPublicKey()
                ? publicKey()
                : addressToPublicKey.apply(address());
        if (publicKey == null || publicKey.readRemaining() != Ed25519.PUBLIC_KEY_LENGTH) {
            return false;
        }
        bytes.readPosition(SIGNATURE);
        bytes.readLimit(readPointer.readLimit());
        return Ed25519.verify(bytes, publicKey);
    }

    @Override
    public final int protocol() {
        return protocol;
    }

    /**
     * Sets the protocol for this message.
     *
     * @param protocol to use
     * @return this instance
     * @throws ArithmeticException if the provided protocol is not
     * in the range [1, 65536]
     * @throws IllegalStateException if the messages has been signed
     */
    public final T protocol(int protocol) {
        assertNotSigned();
        this.protocol = ShortUtil.requirePositiveUnsignedShort(protocol);
        return self();
    }

    @Override
    public final int messageType() {
        return messageType;
    }

    /**
     * Sets the message type for this message.
     *
     * @param messageType to use
     * @return this instance
     * @throws ArithmeticException if the provided message type is not
     * in the range [1, 65536]
     */
    public final T messageType(int messageType) {
        this.messageType = ShortUtil.requirePositiveUnsignedShort(messageType);
        return self();
    }

    /**
     * Returns the ByteBuffer view of this message's binary content. As a
     * side effect, resets the internal ByteBuffer view.
     * <p>
     * The message must be signed before this method is invoked
     * <p>
     * There is only a single view of this message's binary content.
     *
     * @return a ByteBuffer view of the signed content of this message
     * @throws IllegalStateException if the message has not been signed.
     */
    public final ByteBuffer byteBuffer() {
        assertSigned();

        if (byteBuffer == null)
            byteBuffer = ByteBuffer.allocateDirect(0);
        try {
            BB_ADDRESS.setLong(byteBuffer, readPointer.addressForRead(0));
            BB_CAPACITY.setInt(byteBuffer, Math.toIntExact(readPointer.readRemaining()));
            byteBuffer.clear(); // position = 0, limit = capacity.
            return byteBuffer;
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public final <M extends Marshallable> M copyTo(@NotNull M m) {
        assertSameClassAsThis(m);
        assertSigned();
        @SuppressWarnings("unchecked")
        final T other = (T) super.copyTo(m);
        transientFieldHandler().copy(self(), other);
        return m;
    }

    @NotNull
    @Override
    public final <U> U deepCopy() {
        final T copy = super.deepCopy();
        transientFieldHandler().deepCopy(self(), copy);
        return (U) copy;
    }

    /// Overloaded hash, toString etc.


    /**
     * Asserts that the provided instance class is the same as this class and
     * that the provided instance is not null.
     *
     * @param that instance to check
     * @throws IllegalArgumentException if the provided instance class is not
     *                                  the same as this class
     *
     * @throws NullPointerException     if the provided object is null
     */
    private final void assertSameClassAsThis(Object that) {
        if (!this.getClass().equals(that.getClass())) {
            throw new IllegalArgumentException("Class " + that.getClass().getName() + " is not of class " + this.getClass().getName());
        }
    }

    protected final void assertSigned() {
        if (!signed()) {
            throw new IllegalStateException("The message has not been signed");
        }
    }

    protected final void assertNotSigned() {
        if (signed()) {
            throw new IllegalStateException("The message has already been signed");
        }
    }

    @SuppressWarnings("unchecked")
    T self() {
        return (T) this;
    }

}
