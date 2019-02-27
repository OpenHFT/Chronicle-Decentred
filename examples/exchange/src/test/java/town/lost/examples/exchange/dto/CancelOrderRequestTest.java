package town.lost.examples.exchange.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.Marshallable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


final class CancelOrderRequestTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    void marshallable() {
        BytesStore privateKey1 = DecentredUtil.testPrivateKey(7);
        Bytes publicKey1 = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey1 = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey1, secretKey1, privateKey1);

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