package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.api.AccountManagementRequests;
import net.openhft.chronicle.decentred.api.ConnectionStatusListener;
import net.openhft.chronicle.decentred.api.SystemMessageListener;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.salt.Ed25519;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransactionBlockEventTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    public void writeMarshallable() {
        BytesStore privateKey = DecentredUtil.testPrivateKey(7);
        Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);

        BytesStore privateKey2 = DecentredUtil.testPrivateKey(17);
        Bytes publicKey2 = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey2 = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey2, secretKey2, privateKey2);

        DtoRegistry<SystemMessages> registry = DtoRegistry.newRegistry(SystemMessages.class)
                .addProtocol(1, SystemMessageListener.class)
                .addProtocol(2, AccountManagementRequests.class)
                .addProtocol(3, ConnectionStatusListener.class);
        @SuppressWarnings("unchecked")
        TransactionBlockEvent<SystemMessages> tbe = registry.create(TransactionBlockEvent.class);
        tbe.timestampUS(1534769584076123L);
        tbe.dtoParser(registry.get());
        tbe.addTransaction(
                registry.create(CreateAddressRequest.class)
                        .sign(secretKey, new SetTimeProvider("2018-08-20T12:53:04.075128")));
        tbe.addTransaction(
                registry.create(CreateAddressRequest.class)
                        .sign(secretKey2, new SetTimeProvider("2018-08-20T12:53:04.075256")));
        tbe.sign(secretKey, new SetTimeProvider("2018-08-20T12:53:04.076123"));
        assertEquals("!TransactionBlockEvent {\n" +
                "  timestampUS: 2018-08-20T12:53:04.076123,\n" +
                "  address: phccofmpy6ci,\n" +
                "  chainAddress: 0.0.0.0:0,\n" +
                "  weekNumber: 0,\n" +
                "  blockNumber: 0,\n" +
                "  transactions: [\n" +
                "    !CreateAddressRequest { timestampUS: 2018-08-20T12:53:04.075128, address: phccofmpy6ci, publicKey: !!binary 9M9t8hyt2kEJmL46Fs+si0VigLTMQt9OafgMm3ljIOg= },\n" +
                "    !CreateAddressRequest { timestampUS: 2018-08-20T12:53:04.075256, address: ud6jbceicts2, publicKey: !!binary TsXED8x8VoxtLgRu7iPaz4aAhfQUtmvee9KRyhDKk+o= }\n" +
                "  ]\n" +
                "}\n", tbe.toString());
        assertEquals(
                "0000 58 01 00 00                                     # length\n" +
                        "0004 ed fd 09 6c 56 74 d4 55 35 a5 74 c1 16 4d 2b e7 # signature start\n" +
                        "0014 b3 ef 85 04 96 04 62 4a 7b ac 5e 07 0e 66 ad 7c\n" +
                        "0024 7a 81 23 60 6d c0 d5 36 a8 2d 90 95 ad b5 c9 06\n" +
                        "0034 03 c1 0f 0f 9f 77 32 66 c4 0a 4b 56 3b f1 17 0f # signature end\n" +
                        "0044 f0 ff                                           # messageType\n" +
                        "0046 ff ff                                           # protocol\n" +
                        "0048    5b f5 de 63 dd 73 05 00                         # timestampUS\n" +
                        "0050    69 f8 0c 9b 79 63 20 e8                         # address\n" +
                        "0058    00 00 00 00 00 00 00 00                         # chainAddress\n" +
                        "0060    00 00                                           # weekNumber\n" +
                        "0062    00 00 00 00                                     # blockNumber\n" +
                        "0066 79 00 00 00 a3 a7 8c 44 79 1b 1e 7d 3c 0a fc c0\n" +
                        "0076 fd a0 98 f5 69 4d 3f e6 6e fc 1e 2d 34 a5 39 84\n" +
                        "0086 43 41 6d 40 10 03 a6 ee 05 74 02 a9 86 8c de d2\n" +
                        "0096 36 a4 df b8 ea 3a 21 21 c3 ed 0f b5 f9 76 d6 51\n" +
                        "00a6 60 66 40 0e 00 f0 02 00 78 f1 de 63 dd 73 05 00\n" +
                        "00b6 69 f8 0c 9b 79 63 20 e8 20 f4 cf 6d f2 1c ad da\n" +
                        "00c6 41 09 98 be 3a 16 cf ac 8b 45 62 80 b4 cc 42 df\n" +
                        "00d6 4e 69 f8 0c 9b 79 63 20 e8 79 00 00 00 1c db e3\n" +
                        "00e6 7e 08 b9 33 cc 42 35 20 78 8b 27 8e 19 07 4b b8\n" +
                        "00f6 70 7f 63 55 4b 3b 52 03 55 f1 bb e2 f9 8e 7a 2c\n" +
                        "0106 f3 64 3d b9 ea c8 96 82 bf 95 72 3b 6e c8 ab 95\n" +
                        "0116 81 09 29 e0 44 f2 71 9c 3f 2b d9 18 0f 00 f0 02\n" +
                        "0126 00 f8 f1 de 63 dd 73 05 00 7b d2 91 ca 10 ca 93\n" +
                        "0136 ea 20 4e c5 c4 0f cc 7c 56 8c 6d 2e 04 6e ee 23\n" +
                        "0146 da cf 86 80 85 f4 14 b6 6b de 7b d2 91 ca 10 ca\n" +
                        "0156 93 ea\n", tbe.toHexString());
    }
}