package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.salt.Ed25519;

public class KeyPair {
    public final Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
    public final Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);

    public KeyPair(long id) {
        Bytes<Void> privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.zeroOut(0, Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.writeLong(Ed25519.PRIVATE_KEY_LENGTH - Long.BYTES, id);
        privateKey.writeSkip(Ed25519.PRIVATE_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);
        privateKey.release();
    }

    public KeyPair(char ch) {
        Bytes<Void> privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
        while (privateKey.writeRemaining() > 0)
            privateKey.append(ch);
        Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);
        privateKey.release();

    }

}
