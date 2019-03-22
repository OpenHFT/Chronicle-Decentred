package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.salt.Ed25519;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VerificationEventTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    public void publicKey() {
        Bytes<Void> privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.zeroOut(0, privateKey.writeLimit());
        privateKey.writeSkip(privateKey.writeLimit());
        Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);

        Bytes<Void> privateKey2 = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
        privateKey2.zeroOut(0, privateKey2.writeLimit());
        privateKey2.writeSkip(privateKey2.writeLimit());
        privateKey2.writeUnsignedByte(0, 1);
        Bytes publicKey2 = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey2 = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey2, secretKey2, privateKey2);

        assertEquals("00000000 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 ········ ········\n" +
                "........\n" +
                "00000020 3b 6a 27 bc ce b6 a4 2d  62 a3 a8 d0 2a 6f 0d 73 ;j'····- b···*o·s\n" +
                "00000030 65 32 15 77 1d e2 43 a6  3a c0 48 a1 8b 59 da 29 e2·w··C· :·H··Y·)\n", secretKey.toHexString());
        SetTimeProvider timeProvider = new SetTimeProvider(0x05060708090a0bL * 1000);
        VerificationEvent v = new VerificationEvent()
                .protocol(1).messageType(22)
                .keyVerified(publicKey)
                .sign(secretKey2, timeProvider);
        System.out.println(v);

        assertEquals("0000 a2 00 00 00                                     # length\n" +
                "0004 4c 9e 2a 4c 4c c4 02 34 2c 10 d6 8c 61 3d 0f f2 # signature start\n" +
                "0014 69 78 4e b1 e0 a7 a2 d5 1d 85 35 be ed e5 af 76\n" +
                "0024 a4 1b 77 1c 18 2b 70 cf 11 f5 a4 c1 d0 18 fe 96\n" +
                "0034 a8 a8 79 e4 9f 27 9e 92 24 d8 72 af d4 b5 57 08 # signature end\n" +
                "0044 16 00                                           # messageType\n" +
                "0046 01 00                                           # protocol\n" +
                "0048    0b 0a 09 08 07 06 05 00                         # timestampUS\n" +
                "0050    e6 df 06 5d 68 3b d4 fc                         # address\n" +
                "0058    20 ce cc 15 07 dc 1d dd 72 95 95 1c 29 08 88 f0 # publicKey\n" +
                "0068    95 ad b9 04 4d 1b 73 d6 96 e6 df 06 5d 68 3b d4\n" +
                "0078    fc 3a c0 48 a1 8b 59 da 29                      # addressVerified\n" +
                "0081    20 3b 6a 27 bc ce b6 a4 2d 62 a3 a8 d0 2a 6f 0d # keyVerified\n" +
                "0091    73 65 32 15 77 1d e2 43 a6 3a c0 48 a1 8b 59 da\n" +
                "00a1    29\n", v.toHexString());

        assertEquals("!VerificationEvent {\n" +
                "  timestampUS: 2014-10-22T18:22:32.901131,\n" +
                "  address: oyua2manpmw7f,\n" +
                "  publicKey: !!binary zswVB9wd3XKVlRwpCIjwla25BE0bc9aW5t8GXWg71Pw=,\n" +
                "  addressVerified: bsvryqnptqpaz,\n" +
                "  keyVerified: !!binary O2onvM62pC1io6jQKm8Nc2UyFXcd4kOmOsBIoYtZ2ik=\n" +
                "}\n", v.toString());

        assertTrue(v.verify(i -> privateKey2));

        InvalidationEvent ie = new InvalidationEvent()
                .protocol(1).messageType(2)
                .sign(secretKey2, timeProvider);
        System.out.println(ie);
    }
}