package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.salt.Ed25519;

public enum DecentredUtil {
    ;

    public static long toAddress(BytesStore publicKey) {
        return publicKey.readLong(Ed25519.PUBLIC_KEY_LENGTH - Long.BYTES);
    }

    public static BytesStore testPrivateKey(long seed) {
        Bytes privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.zeroOut(0, Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.writeSkip(Ed25519.PRIVATE_KEY_LENGTH - Long.BYTES);
        privateKey.writeLong(seed);
        return privateKey;
    }
}
