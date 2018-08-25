package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.wire.TextMethodTester;
import org.junit.Test;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class VanillaGatewayTest {

    public static void test(String basename) {
        try {
            for (String file : "setup.yaml,in.yaml,out.yaml".split(",")) {
                String path = "src/test/resources/" + basename;
                new File(path).mkdirs();
                try (Closeable c = new FileOutputStream(path + "/" + file, true)) {
                }
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        TextMethodTester<SystemMessages> tester = new TextMethodTester<>(
                basename + "/in.yaml",
                VanillaGatewayTest::createGateway,
                SystemMessages.class,
                basename + "/out.yaml");
        tester.setup(basename + "/setup.yaml");
        try {
            tester.run();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        assertEquals(tester.expected(), tester.actual());
    }

    static VanillaGateway createGateway(SystemMessages tester) {
        VanillaTransactionProcessor blockchain = new VanillaTransactionProcessor(tester);
        return new VanillaGateway(0, 0, tester, tester);
    }

    @Test
    public void createAccountRequest() {
        test("gateway/createAccountRequest");
    }

    @Test
    public void verificationEvent() {
        test("gateway/verificationEvent");
    }

    @Test
    public void invalidationEvent() {
        test("gateway/invalidationEvent");
    }

    @Test
    public void transactionBlockEvent() {
        test("gateway/transactionBlockEvent");
    }

    @Test
    public void transactionBlockGossipEvent() {
        test("gateway/transactionBlockGossipEvent");
    }

    @Test
    public void transactionBlockVoteEvent() {
        test("gateway/transactionBlockVoteEvent");
    }

    @Test
    public void endOfRoundBlockEvent() {
        test("gateway/endOfRoundBlockEvent");
    }

    @Test
    public void createAccountEvent() {
        test("gateway/createAccountEvent");
    }
}
