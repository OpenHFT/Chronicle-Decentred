package town.lost.examples.exchange.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.Marshallable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OpeningBalanceEventTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    public void balances() {
        BytesStore privateKey1 = DecentredUtil.testPrivateKey(7);
        Bytes publicKey1 = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey1 = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey1, secretKey1, privateKey1);

        OpeningBalanceEvent obe = new OpeningBalanceEvent()
                .protocol(1).messageType(1)
                .balanceAddress(DecentredUtil.toAddress(publicKey1));
        obe.balances().put(Currency.XCL, 128.0);
        obe.balances().put(Currency.USD, 10000.0);
        obe.balances().put(Currency.KRW, 11_000_000.0);
        obe.sign(secretKey1, new SetTimeProvider("2018-08-20T11:31:15.379010"));

        assertEquals("!OpeningBalanceEvent {\n" +
                "  timestampUS: 2018-08-20T11:31:15.37901,\n" +
                "  address: phccofmpy6ci,\n" +
                "  balanceAddress: phccofmpy6ci,\n" +
                "  balances: {\n" +
                "    ? XCL: 128.0,\n" +
                "    ? USD: 10E3,\n" +
                "    ? KRW: 11E6\n" +
                "  }\n" +
                "}\n", obe.toString());

        OpeningBalanceEvent obe2 = Marshallable.fromString(obe.toString());
        assertEquals(obe2, obe);
    }
}