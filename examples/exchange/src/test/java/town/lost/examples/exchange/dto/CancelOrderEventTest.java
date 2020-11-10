package town.lost.examples.exchange.dto;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.Marshallable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CancelOrderEventTest {
    static {
        DtoAliases.addAliases();
    }
    @Test
    void marshallable() {
        KeyPair kp7 = new KeyPair(7);
        BytesStore publicKey1 = kp7.publicKey;
        BytesStore secretKey1 = kp7.secretKey;

        CancelOrderEvent cor = new CancelOrderEvent()
                .orderTimestampUS(new SetTimeProvider("2018-03-04T18:03:03.311111").currentTimeMicros())
                .orderAddress(DecentredUtil.parseAddress("bye.now"))
                .protocol(1)
                .messageType(1004)
                .sign(secretKey1, new SetTimeProvider("2018-03-04T18:03:05.644532"));

        assertEquals("!CancelOrderEvent {\n" +
                "  timestampUS: 2018-03-04T18:03:05.644532,\n" +
                "  address: nphccofmpy6ci,\n" +
                "  orderTimestampUS: 2018-03-04T18:03:03.311111,\n" +
                "  orderAddress: bye.now\n" +
                "}\n", cor.toString());
        CancelOrderEvent cor2 = Marshallable.fromString(cor.toString());
        assertEquals(cor2, cor);
    }

}