package town.lost.examples.appreciation;

import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.TextMethodTester;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public final class TestUtils {
    private TestUtils(){}

    public static void test(String basename) {
        TextMethodTester<AppreciationTester> tester = new TextMethodTester<>(
                basename + "/in.yaml",
                TestUtils::createGateway,
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

    public static VanillaAppreciationGateway createGateway(AppreciationTester tester) {
        VanillaBalanceStore balanceStore = new VanillaBalanceStore();
        VanillaAppreciationTransactions blockchain = new VanillaAppreciationTransactions(tester, balanceStore);
        return new VanillaAppreciationGateway(tester, blockchain, balanceStore);
    }

}
