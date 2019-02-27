package town.lost.examples.appreciation;

import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.TextMethodTester;
import town.lost.examples.appreciation.util.VanillaBalanceStore;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public enum TestUtils {
    ;

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
        return new VanillaAppreciationGateway(0, null, null, tester, blockchain, balanceStore);
    }

}
