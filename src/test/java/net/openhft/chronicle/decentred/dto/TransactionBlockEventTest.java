package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.api.AccountManagementRequests;
import net.openhft.chronicle.decentred.api.AnySystemMessage;
import net.openhft.chronicle.decentred.api.ConnectionStatusListener;
import net.openhft.chronicle.decentred.api.SystemMessageListener;
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

        TransactionBlockEvent<AnySystemMessage> tbe = new TransactionBlockEvent<AnySystemMessage>()
                .timestampUS(1534769584076123L);
        DtoRegistry<AnySystemMessage> registry = DtoRegistry.newRegistry(AnySystemMessage.class)
                .addProtocol(1, SystemMessageListener.class)
                .addProtocol(2, AccountManagementRequests.class)
                .addProtocol(3, ConnectionStatusListener.class);
        tbe.dtoParser(registry.get());
        tbe.addTransaction(
                registry.create(CreateAccountRequest.class)
                        .sign(secretKey, new SetTimeProvider("2018-08-20T12:53:04.075128")));
        tbe.addTransaction(
                registry.create(CreateAccountRequest.class)
                        .sign(secretKey2, new SetTimeProvider("2018-08-20T12:53:04.075256")));
//        tbe.sign(secretKey);
        assertEquals("!TransactionBlockEvent {\n" +
                "  timestampUS: 2018-08-20T12:53:04.076123,\n" +
                "  address: 0.0.0.0:0,\n" +
                "  chainId: 0,\n" +
                "  weekNumber: 0,\n" +
                "  blockNumber: 0,\n" +
                "  transactions: [\n" +
                "    !CreateAccountRequest { timestampUS: 2018-08-20T12:53:04.075128, address: phccofmpy6ci, publicKey: !!binary 9M9t8hyt2kEJmL46Fs+si0VigLTMQt9OafgMm3ljIOg= },\n" +
                "    !CreateAccountRequest { timestampUS: 2018-08-20T12:53:04.075256, address: ud6jbceicts2, publicKey: !!binary TsXED8x8VoxtLgRu7iPaz4aAhfQUtmvee9KRyhDKk+o= }\n" +
                "  ]\n" +
                "}\n", tbe.toString());
    }
}