package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.salt.Ed25519;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DecentredUtilTest {

    public static void main(String[] args) {
        // generate user addresses
        for (int i = 1; i < 1000; i++) {
            BytesStore privateKey = DecentredUtil.testPrivateKey(i);
            Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
            Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
            Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);
            long address = DecentredUtil.toAddress(publicKey);
            if ((address | ~DecentredUtil.ADDRESS_MASK) == address) {
                System.out.print(i + "\t");
            }
        }
    }

    @Test
    public void testPrivateKey() {
        BytesStore privateKey = DecentredUtil.testPrivateKey(0);
        Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);

        BytesStore privateKey2 = DecentredUtil.testPrivateKey(0);
        Bytes publicKey2 = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey2 = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey2, secretKey2, privateKey2);
        assertEquals(privateKey2, privateKey);
        assertEquals(publicKey2, publicKey);
        assertEquals(secretKey2, secretKey);
    }
}