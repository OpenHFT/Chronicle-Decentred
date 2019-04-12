package town.lost.examples.exchange.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.Marshallable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TradeEventTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    void marshallable() {
        KeyPair keyPair = new KeyPair(17);

        TradeEvent te = new TradeEvent()
                .action(Side.BUY)
                .currencyPair(CurrencyPair.EURXCL)
                .price(1000.0)
                .quantity(1000.0)
                .orderAddress(DecentredUtil.parseAddress("abcdefghijk"))
                .orderTimestampUS(new SetTimeProvider("2018-03-04T18:03:04.264453").currentTimeMicros())
                .protocol(1)
                .messageType(1002)
                .sign(keyPair.secretKey, new SetTimeProvider("2018-03-04T18:03:05.364453"));
        assertEquals("!TradeEvent {\n" +
                "  timestampUS: 2018-03-04T18:03:05.364453,\n" +
                "  address: nud6jbceicts2,\n" +
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