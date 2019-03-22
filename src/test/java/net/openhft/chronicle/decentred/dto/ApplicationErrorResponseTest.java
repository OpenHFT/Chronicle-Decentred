package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.salt.Ed25519;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationErrorResponseTest {
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

        SetTimeProvider timeProvider = new SetTimeProvider(0x05060708090a0bL * 1000);
        CreateAddressRequest ca = new CreateAddressRequest()
                .protocol(1).messageType(2)
                .sign(secretKey, timeProvider);

        ApplicationErrorResponse ae = new ApplicationErrorResponse()
                .protocol(1).messageType(10)
                .init(ca, "Not implemented")
                .sign(secretKey, timeProvider);

        assertEquals(
                "0000 e5 00 00 00                                     # length\n" +
                        "0004 e0 28 8f 34 ac b1 26 8c 17 76 36 ad f1 25 23 68 # signature start\n" +
                        "0014 cd 3a 5d 99 1a 2f 82 2f 2e e1 a2 a6 e2 30 6f b3\n" +
                        "0024 ee de 7e e1 5d c8 01 b4 02 88 48 ee 95 0f 88 d3\n" +
                        "0034 29 f2 7f a4 e2 0c 48 18 dd 7f ae 75 e1 6a 20 07 # signature end\n" +
                        "0044 0a 00                                           # messageType\n" +
                        "0046 01 00                                           # protocol\n" +
                        "0048    0b 0a 09 08 07 06 05 00                         # timestampUS\n" +
                        "0050    3a c0 48 a1 8b 59 da 29                         # address\n" +
                        "0058    0f 4e 6f 74 20 69 6d 70 6c 65 6d 65 6e 74 65 64 # reason\n" +
                        "0068 79 00 00 00                                     # origMessage.length\n" +
                        "006c 79 00 00 00 96 de da 9f 15 2c 01 e1 93 0e 3f 49 # origMessage\n" +
                        "007c 14 4f d5 88 90 03 38 f7 6a 37 e8 32 8d 59 88 39\n" +
                        "008c 7c 9c 30 0c 1c 6f 8f fd b5 66 fd d1 a6 56 41 ee\n" +
                        "009c 37 dc ef df 33 a1 95 3c 0e 6b 1d 7b 2f bd bd 44\n" +
                        "00ac fc 42 97 0b 02 00 01 00 0b 0a 09 08 07 06 05 00\n" +
                        "00bc 3a c0 48 a1 8b 59 da 29 20 3b 6a 27 bc ce b6 a4\n" +
                        "00cc 2d 62 a3 a8 d0 2a 6f 0d 73 65 32 15 77 1d e2 43\n" +
                        "00dc a6 3a c0 48 a1 8b 59 da 29\n",
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
