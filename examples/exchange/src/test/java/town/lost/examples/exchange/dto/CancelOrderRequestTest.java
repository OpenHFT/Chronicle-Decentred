package town.lost.examples.exchange.dto;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.Marshallable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CancelOrderRequestTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    public void marshallable() {
        KeyPair kp7 = new KeyPair(7);
        BytesStore publicKey1 = kp7.publicKey;
        BytesStore secretKey1 = kp7.secretKey;

        CancelOrderRequest cor = new CancelOrderRequest()
                .orderTimestampUS(new SetTimeProvider("2018-03-04T18:03:05.364453").currentTimeMicros())
                .protocol(1)
                .messageType(1002)
                .currencyPair(CurrencyPair.USDXCL)
                .sign(secretKey1, new SetTimeProvider("2018-03-04T18:03:05.644532"));

        assertEquals("!CancelOrderRequest {\n" +
                "  timestampUS: 2018-03-04T18:03:05.644532,\n" +
                "  address: phccofmpy6ci,\n" +
                "  currencyPair: USDXCL,\n" +
                "  orderTimestampUS: 2018-03-04T18:03:05.364453\n" +
                "}\n", cor.toString());
        CancelOrderRequest cor2 = Marshallable.fromString(cor.toString());
        assertEquals(cor2, cor);
    }

}