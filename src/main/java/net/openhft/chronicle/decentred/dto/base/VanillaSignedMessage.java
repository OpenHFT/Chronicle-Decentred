package net.openhft.chronicle.decentred.dto.base;

import net.openhft.chronicle.bytes.*;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.decentred.util.ShortUtil;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.AbstractBytesMarshallable;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.MicroTimestampLongConverter;
import net.openhft.chronicle.wire.WireIn;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.function.LongFunction;

import static java.util.Objects.requireNonNull;

public class VanillaSignedMessage<T extends VanillaSignedMessage<T>> extends AbstractBytesMarshallable implements SignedMessage {
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
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        signed = false;
        super.readMarshallable(wire);
    }

    @Override
    public void readMarshallable(BytesIn bytes) throws IORuntimeException {
        long capacity = bytes.readRemaining();
        readPointer.set(bytes.addressForRead(bytes.readPosition()), capacity);
        messageType = readPointer.readUnsignedShort(MESSAGE_TYPE);
        protocol = readPointer.readUnsignedShort(PROTOCOL);

        this.bytes.clear();
        this.bytes.readPositionRemaining(MESSAGE_START, capacity - MESSAGE_START);
        super.readMarshallable(this.bytes);
        signed = true;
    }

    /**
     * Resets all properties of this message to their default values. This sets
     * the message in the same state as if it was just created.
     */
    public void reset() {
        signed = false;
        messageType = 0;
        protocol = 0;
        // address = timestampUS = 0; set by super.reset();
        super.reset();
    }

    @Override
    public boolean signed() {
        return signed;
    }

    @Override
    public void writeMarshallable(BytesOut bytes) {
        assertSigned();
        bytes.write(this.bytes, 0, this.bytes.readLimit());
    }

    protected void writeMarshallable0(BytesOut bytes) {
        super.writeMarshallable(bytes);
    }

    @Override
    public long address() {
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
    public T address(long address) {
        assertNotSigned();
        this.address = address;
        return (T) this;
    }

    @Override
    public long timestampUS() {
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
    public T timestampUS(long timestampUS) {
        assertNotSigned();
        this.timestampUS = timestampUS;
        return (T) this;
    }

     // Signifies this message contains it's own public key.
    @Override
    public BytesStore publicKey() {
        return NoBytesStore.noBytesStore();
    }

    public boolean hasPublicKey() {
        return false;
    }

    public T publicKey(BytesStore key) {
        throw new UnsupportedOperationException("This method is not supported for this message type. Only for " + SelfSignedMessage.class.getSimpleName());
    }

    @Override
    public T sign(BytesStore secretKey) {
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
     * @throws NullPointerException if any of the provided parameters
     * are {@code null}
     */
    public T sign(BytesStore secretKey, TimeProvider timeProvider) {
        requireNonNull(secretKey);
        requireNonNull(timeProvider);
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
        writeMarshallable0(tempBytes);
        long length = tempBytes.readRemaining();
        tempBytes.writeUnsignedInt(LENGTH, length);
        tempBytes.readPosition(signatureStart);
        Ed25519.sign(tempBytes, secretKey);
        signed = true;
        readPointer.set(tempBytes.addressForRead(0), length);
        bytes.writeLimit(length)
                .readPositionRemaining(0, length);
        return (T) this;
    }

    public String toHexString() {
        HexDumpBytes dump = new HexDumpBytes()
                .offsetFormat((o, b) -> b.appendBase16(o, 4));
        dump.comment("length").writeUnsignedInt(bytes.readUnsignedInt(LENGTH));
        dump.comment("signature start").write(bytes, (long) SIGNATURE, Ed25519.SIGNATURE_LENGTH);
        dump.comment("signature end");
        dump.comment("messageType").writeUnsignedShort(messageType);
        dump.comment("protocol").writeUnsignedShort(protocol);
        writeMarshallable0(dump);
        String text = dump.toHexString();
        dump.release();
        return text;
    }

    public boolean verify(LongFunction<BytesStore> addressToPublicKey) {
        BytesStore publicKey = hasPublicKey()
                ? publicKey()
                : addressToPublicKey.apply(address());
        if (publicKey == null || publicKey.readRemaining() != Ed25519.PUBLIC_KEY_LENGTH)
            return false;

        bytes.readPosition(SIGNATURE);
        bytes.readLimit(readPointer.readLimit());
        return Ed25519.verify(bytes, publicKey);
    }

    @Override
    public int protocol() {
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
    public T protocol(int protocol) {
        assertNotSigned();
        this.protocol = ShortUtil.requirePositiveUnsignedShort(protocol);
        return (T) this;
    }

    @Override
    public int messageType() {
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
    public T messageType(int messageType) {
        this.messageType = ShortUtil.requirePositiveUnsignedShort(messageType);
        return (T) this;
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
    public ByteBuffer byteBuffer() {
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

    protected void assertSigned() {
        if (!signed()) {
            throw new IllegalStateException("The message has not been signed");
        }
    }

    protected void assertNotSigned() {
        if (signed()) {
            throw new IllegalStateException("The message has already been signed");
        }
    }

}
