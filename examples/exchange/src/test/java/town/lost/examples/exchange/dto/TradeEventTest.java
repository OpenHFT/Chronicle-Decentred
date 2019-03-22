package town.lost.examples.exchange.dto;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.Marshallable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TradeEventTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    public void marshallable() {
        KeyPair kp7 = new KeyPair(7);
        BytesStore publicKey1 = kp7.publicKey;
        BytesStore secretKey1 = kp7.secretKey;

        TradeEvent te = new TradeEvent()
                .action(Side.BUY)
                .currencyPair(CurrencyPair.EURXCL)
                .price(1000.0)
                .quantity(1000.0)
                .orderAddress(DecentredUtil.parseAddress("abcdefghijk"))
                .orderTimestampUS(new SetTimeProvider("2018-03-04T18:03:04.264453").currentTimeMicros())
                .protocol(1)
                .messageType(1002)
                .sign(secretKey1, new SetTimeProvider("2018-03-04T18:03:05.364453"));
        assertEquals("!TradeEvent {\n" +
                "  timestampUS: 2018-03-04T18:03:05.364453,\n" +
                "  address: phccofmpy6ci,\n" +
                "  orderTimestampUS: 2018-03-04T18:03:04.264453,\n" +
                "  orderAddress: abcdefghijk,\n" +
                "  quantity: 1E3,\n" +
                "  price: 1E3,\n" +
                "  currencyPair: EURXCL,\n" +
                "  side: BUY\n" +
                "}\n", te.toString());
        TradeEvent te2 = Marshallable.fromString(te.toString());
        assertEquals(te2, te);
    }

}