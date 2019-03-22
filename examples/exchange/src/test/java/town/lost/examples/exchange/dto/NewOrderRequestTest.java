package town.lost.examples.exchange.dto;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.Marshallable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NewOrderRequestTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    public void marshallable() {
        KeyPair kp7 = new KeyPair(7);
        BytesStore publicKey1 = kp7.publicKey;
        BytesStore secretKey1 = kp7.secretKey;

        NewOrderRequest nor = new NewOrderRequest()
                .side(Side.BUY)
                .currencyPair(CurrencyPair.EURXCL)
                .maxPrice(1000.0)
                .quantity(1000.0)
                .ttlMillis(300)
                .protocol(1)
                .messageType(1001)
                .sign(secretKey1, new SetTimeProvider("2018-03-04T18:03:05.364453"));
        assertEquals("!NewOrderRequest {\n" +
                "  timestampUS: 2018-03-04T18:03:05.364453,\n" +
                "  address: phccofmpy6ci,\n" +
                "  side: BUY,\n" +
                "  quantity: 1E3,\n" +
                "  maxPrice: 1E3,\n" +
                "  currencyPair: EURXCL,\n" +
                "  ttlMillis: 300\n" +
                "}\n", nor.toString());
        NewOrderRequest nor2 = Marshallable.fromString(nor.toString());
        assertEquals(nor2, nor);
    }

}