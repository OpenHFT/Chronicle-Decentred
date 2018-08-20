package town.lost.examples.appreciation;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.dto.CreateAccountRequest;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.UniqueMicroTimeProvider;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.TextMethodTester;
import net.openhft.chronicle.wire.TextWire;
import org.junit.Test;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OpeningBalance;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class GiveTest {
    public static void test(String basename) {
        TextMethodTester<AppreciationTester> tester = new TextMethodTester<>(
                basename + "/in.yaml",
                GiveTest::createGateway,
                AppreciationTester.class,
                basename + "/out.yaml");
        tester.setup(basename + "/setup.yaml");
        try {
            tester.run();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        assertEquals(tester.expected(), tester.actual());
    }

    private static VanillaAppreciationGateway createGateway(AppreciationTester tester) {
        VanillaBalanceStore balanceStore = new VanillaBalanceStore();
        VanillaAppreciationTransactions blockchain = new VanillaAppreciationTransactions(tester, balanceStore);
        return new VanillaAppreciationGateway(tester,
                blockchain,
                balanceStore);
    }

    public static void main(String[] args) {
        BytesStore privateKey1 = DecentredUtil.testPrivateKey(7);
        Bytes publicKey1 = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey1 = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey1, secretKey1, privateKey1);

        BytesStore privateKey2 = DecentredUtil.testPrivateKey(17);
        Bytes publicKey2 = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey2 = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey2, secretKey2, privateKey2);

        TextWire wire = new TextWire(Bytes.elasticHeapByteBuffer(128));
        AppreciationTester tester = wire.methodWriter(AppreciationTester.class);
        tester.createAccountRequest(new CreateAccountRequest()
                .address(0)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .publicKey(publicKey1));
        tester.createAccountRequest(new CreateAccountRequest()
                .address(0)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .publicKey(publicKey2));

        long address1 = DecentredUtil.toAddress(publicKey1);
        long address2 = DecentredUtil.toAddress(publicKey2);
        tester.openingBalance(new OpeningBalance()
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .init(address1, 100));
        tester.openingBalance(new OpeningBalance()
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .init(address2, 20));

        tester.give(new Give()
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .address(address1)
                .init(address2, 0.0));

        System.out.println(wire);
    }

    @Test
    public void testGiveOne() {
        test("give/one");
    }
}
