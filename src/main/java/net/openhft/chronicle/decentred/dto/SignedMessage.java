package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.BytesMarshallable;
import net.openhft.chronicle.bytes.BytesStore;

public interface SignedMessage extends BytesMarshallable {
    int protocol();

    int messageType();

    /**
     * Once a message is signed it can't be modified, only read.
     * <p>
     * It cannot be sent until it is signed.
     *
     * @return true if signed, false if not signed
     */
    boolean signed();

    /**
     * Unique address for the sender. This should be the last 8 bytes of the public key.
     *
     * @return a unique id for this server.
     */
    long address();

    /**
     * A microsecond precision, unique timestamp
     * <p>
     * NOTE: If the writing system doesn't have a micro-second accurate clock,
     * the most accurate clock should be used and increment the timestamp in the cause of any collision
     *
     * @return a unique microsecond timestamp for an address. Must be monotonically increasing.
     */
    long timestampUS();

    /**
     * @return the public key if it is embedded in the message, otherwise it will need to be implied from the address.
     */
    BytesStore publicKey();

    /**
     * Signs and returns this message. As an additional side effect, also sets
     * the public key (if any) and address in this message.
     *
     * @param secretKey to sign this message with.
     * @return this signed message
     * @throws NullPointerException if the provided {@code secretKey} is {@code null}
     */
    SignedMessage sign(BytesStore secretKey);

}
