package net.openhft.chronicle.decentred.dto.base;

import net.openhft.chronicle.bytes.*;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.base.trait.HasDtoParser;
import net.openhft.chronicle.decentred.dto.base.trait.HasTransientFieldHandler;
import net.openhft.chronicle.decentred.internal.util.ShortUtil;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public abstract class VanillaSignedMessage<T extends VanillaSignedMessage<T>> extends AbstractBytesMarshallable
    implements SignedMessage, HasTransientFieldHandler<T> {

    private static final int INITIAL_BYTES_CAPACITY = 4 << 10;
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

    @LongConversion(MicroTimestampLongConverter.class)
    private long timestampUS;
    @LongConversion(AddressLongConverter.class)
    private long address;

    // Byte area for storage of a complete signed message
    private final transient Bytes internalBytes;

    private transient boolean signed = false;
    private transient ByteBuffer byteBuffer;
    // unsigned 16-bit
    private transient int messageType, protocol;

    public VanillaSignedMessage() {
        internalBytes = Bytes.allocateElasticDirect(INITIAL_BYTES_CAPACITY);

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
                .filter(n -> !n.contains("$jacocoData")) // Discard instrumented fields from jacoco
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
        signed = false; // How do we know the state of the signed property?
        super.readMarshallable(wire);
        transientFieldHandler().readMarshallable(self(), wire);
    }

    @Override
    public final void readMarshallable(BytesIn bytes) throws IORuntimeException {
        internalBytes.clear();
        internalBytes.write(bytes);
        // use internalBytes from here since we copied everything.
        // Todo: In the future, we could just reference the incoming bytes, because it should be immutable
        messageType = internalBytes.readUnsignedShort(MESSAGE_TYPE);
        protocol = internalBytes.readUnsignedShort(PROTOCOL);

        internalBytes.readSkip(MESSAGE_START);
        super.readMarshallable(internalBytes);
        signed = true;
        transientFieldHandler().readMarshallable(self(), internalBytes);
        internalBytes.readPosition(0);
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
        bytes.write(internalBytes, 0, internalBytes.readLimit());
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

        internalBytes.clear();
        internalBytes.writeInt(0); // Provisional length
        internalBytes.writeSkip(Ed25519.SIGNATURE_LENGTH);
        internalBytes.writeUnsignedShort(messageType);
        internalBytes.writeUnsignedShort(protocol);
        writeMarshallableInternal(internalBytes);
        long length = internalBytes.readRemaining();
        internalBytes.writeUnsignedInt(LENGTH, length);
        internalBytes.readPosition(SIGNATURE);
        Ed25519.sign(internalBytes, secretKey);
        signed = true;
        //readPointer.set(tempBytes.addressForRead(0), length);
        internalBytes.writeLimit(length);
        internalBytes.readPositionRemaining(0, length);
        return self();
    }

    public final String toHexString() {
        assertSigned();
        HexDumpBytes dump = new HexDumpBytes()
                .offsetFormat((o, b) -> b.appendBase16(o, 4));
        dump.comment("length").writeUnsignedInt(internalBytes.readUnsignedInt(LENGTH));
        dump.comment("signature start").write(internalBytes, (long) SIGNATURE, Ed25519.SIGNATURE_LENGTH);
        dump.comment("signature end");
        dump.comment("messageType").writeUnsignedShort(messageType);
        dump.comment("protocol").writeUnsignedShort(protocol);
        writeMarshallableInternal(dump);
        String text = dump.toHexString();
        dump.release();
        return text;
    }

    public final boolean verify(LongFunction<BytesStore> addressToPublicKey) {
        final BytesStore publicKey = hasPublicKey()
                ? publicKey()
                : addressToPublicKey.apply(address());
        if (publicKey == null || publicKey.readRemaining() != Ed25519.PUBLIC_KEY_LENGTH) {
            return false;
        }
        final Bytes verifyBytes = internalBytes.bytesForRead();
        verifyBytes.readPosition(SIGNATURE);
        verifyBytes.readLimit(verifyBytes.readLimit());
        return Ed25519.verify(verifyBytes, publicKey);
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
            BB_ADDRESS.setLong(byteBuffer, internalBytes.addressForRead(0));
            BB_CAPACITY.setInt(byteBuffer, Math.toIntExact(internalBytes.readRemaining()));
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
        final T other = (T) m;
        // This volatile property gets magically copied
        if (this instanceof HasDtoParser) {
            ((HasDtoParser)m).dtoParser(((HasDtoParser)this).dtoParser());
        }
        final Bytes internalBytesView = internalBytes.bytesForRead();
        internalBytesView.readPosition(0);
        other.readMarshallable(internalBytesView);
        other.transientFieldHandler().copyNonMarshalled((T) this, other);
        return m;
    }

    @NotNull
    @Override
    public final <U> U deepCopy() {
        try {
            @SuppressWarnings("unchecked")
            // Create a new instance of the same type as this
            final T copy = (T) getClass().newInstance();
            // Perform a shallow copyNonMarshalled
            copyTo(copy);
            // Dereference shallow items
            //transientFieldHandler().deepCopy(self(), copyNonMarshalled);
            @SuppressWarnings("unchecked")
            final U result = (U) copy;
            return result;

        } catch (ReflectiveOperationException e) {
            Jvm.rethrow(e);
        }
        throw new IllegalStateException("We should never end up here");
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
            throw new IllegalStateException(
                String.format("The message of type %s, protocol %d, messageType %d has not been signed.", getClass().getSimpleName(), protocol(), messageType())
            );
        }
    }

    protected final void assertNotSigned() {
        if (signed()) {
            throw new IllegalStateException(
                String.format("The message of type %s, protocol %d, messageType %d has already been signed.", getClass().getSimpleName(), protocol(), messageType())
            );
        }
    }

    @SuppressWarnings("unchecked")
    T self() {
        return (T) this;
    }

}
