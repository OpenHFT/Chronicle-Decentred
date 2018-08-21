package town.lost.examples.exchange.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.Marshallable;
import org.junit.Test;
import town.lost.examples.exchange.api.DtoAliases;

import static org.junit.Assert.assertEquals;

public class CancelOrderEventTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    public void marshallable() {
        BytesStore privateKey1 = DecentredUtil.testPrivateKey(7);
        Bytes publicKey1 = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey1 = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey1, secretKey1, privateKey1);

        CancelOrderEvent cor = new CancelOrderEvent()
                .orderTimestampUS(new SetTimeProvider("2018-03-04T18:03:03.311111").currentTimeMicros())
                .orderAddress(DecentredUtil.parseAddress("bye.now"))
                .protocol(1)
                .messageType(1004)
                .sign(secretKey1, new SetTimeProvider("2018-03-04T18:03:05.644532"));

        assertEquals("!CancelOrderEvent {\n" +
                "  timestampUS: 2018-03-04T18:03:05.644532,\n" +
                "  address: phccofmpy6ci,\n" +
                "  orderTimestampUS: 2018-03-04T18:03:03.311111,\n" +
                "  orderAddress: bye.now\n" +
                "}\n", cor.toString());
        CancelOrderEvent cor2 = Marshallable.fromString(cor.toString());
        assertEquals(cor2, cor);
    }

}