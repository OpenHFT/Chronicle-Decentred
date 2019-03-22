package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.AbstractMarshallable;

// TODO Deduplicate with Ed25519.KeyPair
public class KeyPair extends AbstractMarshallable {
    public final BytesStore publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
    public final BytesStore secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);

    /**
     * Generate a new public and secret key pair using a random seed.
     */
    public KeyPair() {
        Ed25519.generatePublicAndSecretKey((Bytes) publicKey, (Bytes) secretKey);
    }

    /**
     * Only use this for testing!
     *
     * @param id to use as a private seed
     */
    public KeyPair(long id) {
        Bytes<Void> privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.zeroOut(0, Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.writeLong(Ed25519.PRIVATE_KEY_LENGTH - (long) Long.BYTES, id);
        privateKey.writeSkip(Ed25519.PRIVATE_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret((Bytes) publicKey, (Bytes) secretKey, privateKey);
        privateKey.release();
    }

    /**
     * Only use this for testing!
     *
     * @param ch to use as a private seed
     */
    @Deprecated
    public KeyPair(char ch) {
        Bytes<Void> privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
        while (privateKey.writeRemaining() > 0)
            privateKey.append(ch);
        Ed25519.privateToPublicAndSecret((Bytes) publicKey, (Bytes) secretKey, privateKey);
        privateKey.release();
    }

    public long address() {
        return DecentredUtil.toAddress(publicKey);
    }
}
