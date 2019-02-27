package town.lost.examples.exchange;

import net.openhft.chronicle.core.annotation.UsedViaReflection;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.TextMethodTester;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public enum TestUtils { ;

    public static void test(String basename) {
        TextMethodTester<ExchangeTester> tester = new TextMethodTester<>(
                basename + "/in.yaml",
                TestUtils::createGateway,
                ExchangeTester.class,
                basename + "/out.yaml");
        tester.setup(basename + "/setup.yaml");
        try {
            tester.run();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        assertEquals(tester.expected(), tester.actual());
    }

    public static ExchangeGateway createGateway(ExchangeTester tester) {
        ExchangeTransactionProcessor blockchain = new ExchangeTransactionProcessor(tester);
        return new ExchangeGateway(tester, blockchain) {
            @Override
            protected boolean privilegedAddress(long address) {
                return address != 0;
            }

            @UsedViaReflection
            public void setCurrentTime(long currentTime) {
                blockchain.setCurrentTime(currentTime);
            }
        };
    }

}
