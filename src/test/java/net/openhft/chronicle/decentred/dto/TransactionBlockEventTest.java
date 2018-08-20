package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.api.*;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.salt.Ed25519;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransactionBlockEventTest {
    static {
        DtoAlias.addAliases();
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

        DtoRegistry<AnySystemMessage> registry = DtoRegistry.newRegistry(AnySystemMessage.class)
                .addProtocol(1, SystemMessageListener.class)
                .addProtocol(2, AccountManagementRequests.class)
                .addProtocol(3, ConnectionStatusListener.class)
                .addProtocol(4, Blockchainer.class);
        TransactionBlockEvent<AnySystemMessage> tbe = registry.create(TransactionBlockEvent.class);
        tbe.timestampUS(1534769584076123L);
        tbe.dtoParser(registry.get());
        tbe.addTransaction(
                registry.create(CreateAccountRequest.class)
                        .sign(secretKey, new SetTimeProvider("2018-08-20T12:53:04.075128")));
        tbe.addTransaction(
                registry.create(CreateAccountRequest.class)
                        .sign(secretKey2, new SetTimeProvider("2018-08-20T12:53:04.075256")));
        tbe.sign(secretKey, new SetTimeProvider("2018-08-20T12:53:04.076123"));
        assertEquals("!TransactionBlockEvent {\n" +
                "  timestampUS: 2018-08-20T12:53:04.076123,\n" +
                "  address: phccofmpy6ci,\n" +
                "  chainId: 0,\n" +
                "  weekNumber: 0,\n" +
                "  blockNumber: 0,\n" +
                "  transactions: [\n" +
                "    !CreateAccountRequest { timestampUS: 2018-08-20T12:53:04.075128, address: phccofmpy6ci, publicKey: !!binary 9M9t8hyt2kEJmL46Fs+si0VigLTMQt9OafgMm3ljIOg= },\n" +
                "    !CreateAccountRequest { timestampUS: 2018-08-20T12:53:04.075256, address: ud6jbceicts2, publicKey: !!binary TsXED8x8VoxtLgRu7iPaz4aAhfQUtmvee9KRyhDKk+o= }\n" +
                "  ]\n" +
                "}\n", tbe.toString());
        assertEquals(
                "0000 52 01 00 00                                     # length\n" +
                        "0004 62 63 73 6d f7 21 2d 86 be 51 dd 5c c6 df ac c6 # signature start\n" +
                        "0014 04 f4 7f e1 30 49 04 d1 b4 91 73 a1 e2 c6 fa 60\n" +
                        "0024 f7 0b a2 24 24 0b 68 87 fa be a2 2a 26 b7 37 39\n" +
                        "0034 45 98 29 76 46 cf 3c 55 1a ae 3a bd 63 b0 47 05 # signature end\n" +
                        "0044 00 02                                           # messageType\n" +
                        "0046 04 00                                           # protocol\n" +
                        "0048    5b f5 de 63 dd 73 05 00                         # timestampUS\n" +
                        "0050    69 f8 0c 9b 79 63 20 e8                         # address\n" +
                        "0058    00 00                                           # chainId\n" +
                        "005a    00 00                                           # weekNumber\n" +
                        "005c    00 00 00 00                                     # blockNumber\n" +
                        "0060 79 00 00 00 c7 14 5b d7 e5 80 6b 95 7d 77 d5 f8\n" +
                        "0070 cc 78 9d 2f 89 28 15 0d 9e ad 2c d9 dc f1 d7 7c\n" +
                        "0080 fe e3 81 35 c0 b0 2c d2 4f b7 6e 79 43 3e d9 d9\n" +
                        "0090 4d fa 7f 91 7f e7 92 4e 4d b9 8a 45 3a a0 89 ba\n" +
                        "00a0 98 46 c2 00 00 01 02 00 78 f1 de 63 dd 73 05 00\n" +
                        "00b0 69 f8 0c 9b 79 63 20 e8 20 f4 cf 6d f2 1c ad da\n" +
                        "00c0 41 09 98 be 3a 16 cf ac 8b 45 62 80 b4 cc 42 df\n" +
                        "00d0 4e 69 f8 0c 9b 79 63 20 e8 79 00 00 00 b0 ed f6\n" +
                        "00e0 04 06 2a 47 40 9c fb 7e f3 51 ef 68 a7 ee b4 49\n" +
                        "00f0 a9 1e ae 07 6d 6d 91 e2 82 e8 16 b3 e5 c8 15 19\n" +
                        "0100 bf 1f 7a d7 e6 22 70 e5 16 0a 2f 4e bf 16 40 51\n" +
                        "0110 d1 c2 49 4f ca ec 08 69 af 79 aa d8 04 00 01 02\n" +
                        "0120 00 f8 f1 de 63 dd 73 05 00 7b d2 91 ca 10 ca 93\n" +
                        "0130 ea 20 4e c5 c4 0f cc 7c 56 8c 6d 2e 04 6e ee 23\n" +
                        "0140 da cf 86 80 85 f4 14 b6 6b de 7b d2 91 ca 10 ca\n" +
                        "0150 93 ea\n", tbe.toHexString());
    }
}