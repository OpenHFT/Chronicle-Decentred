package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.base.DtoAliases;
import net.openhft.chronicle.decentred.dto.error.ApplicationErrorResponse;
import net.openhft.chronicle.salt.Ed25519;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ApplicationErrorResponseTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    void publicKey() {
        Bytes<Void> privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.zeroOut(0, privateKey.writeLimit());
        privateKey.writeSkip(privateKey.writeLimit());
        Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);

        SetTimeProvider timeProvider = new SetTimeProvider(0x05060708090a0bL * 1000);
        CreateAddressRequest ca = new CreateAddressRequest()
                .protocol(1).messageType(2)
                .sign(secretKey, timeProvider);

        ApplicationErrorResponse ae = new ApplicationErrorResponse()
                .protocol(1).messageType(10)
                .init(ca, "Not implemented")
                .sign(secretKey, timeProvider);

        assertEquals(
                "0000 e1 00 00 00                                     # length\n" +
                        "0004 cc 7f 8f b0 4d 0e e4 d8 7b 8b 77 78 81 da c2 a5 # signature start\n" +
                        "0014 94 e9 27 1b f1 5b 2e b4 95 4c 4b 7b 5b 78 92 79\n" +
                        "0024 94 4e 5a 62 8a 57 b5 44 92 53 6a 89 8b 6c e5 ae\n" +
                        "0034 d1 2a bd 8b 04 80 18 ce 71 5f b2 38 81 df de 00 # signature end\n" +
                        "0044 0a 00                                           # messageType\n" +
                        "0046 01 00                                           # protocol\n" +
                        "0048    0b 0a 09 08 07 06 05 00                         # timestampUS\n" +
                        "0050    3a c0 48 a1 8b 59 da 29                         # address\n" +
                        "0058    0f 4e 6f 74 20 69 6d 70 6c 65 6d 65 6e 74 65 64 # reason\n" +
                        "0068 79 00 00 00 96 de da 9f 15 2c 01 e1 93 0e 3f 49\n" +
                        "0078 14 4f d5 88 90 03 38 f7 6a 37 e8 32 8d 59 88 39\n" +
                        "0088 7c 9c 30 0c 1c 6f 8f fd b5 66 fd d1 a6 56 41 ee\n" +
                        "0098 37 dc ef df 33 a1 95 3c 0e 6b 1d 7b 2f bd bd 44\n" +
                        "00a8 fc 42 97 0b 02 00 01 00 0b 0a 09 08 07 06 05 00\n" +
                        "00b8 3a c0 48 a1 8b 59 da 29 20 3b 6a 27 bc ce b6 a4\n" +
                        "00c8 2d 62 a3 a8 d0 2a 6f 0d 73 65 32 15 77 1d e2 43\n" +
                        "00d8 a6 3a c0 48 a1 8b 59 da 29\n",
                ae.toHexString());

        assertEquals("!ApplicationErrorResponse {\n" +
                "  timestampUS: 2014-10-22T18:22:32.901131,\n" +
                "  address: bsvryqnptqpaz,\n" +
                "  reason: Not implemented,\n" +
                "  origMessage: !CreateAddressRequest {\n" +
                "    timestampUS: 2014-10-22T18:22:32.901131,\n" +
                "    address: bsvryqnptqpaz,\n" +
                "    publicKey: !!binary O2onvM62pC1io6jQKm8Nc2UyFXcd4kOmOsBIoYtZ2ik=\n" +
                "  }\n" +
                "}\n", ae.toString());
        ae.verify(i -> publicKey);
    }
}
