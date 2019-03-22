package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.DtoAliases;
import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.TextMethodTester;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class VanillaGatewayTest {
    static {
        DtoAliases.addAliases();
    }

    public static void test(String basename) {
/*        try {
            for (String file : "setup.yaml,in.yaml,out.yaml".split(",")) {
                String path = "src/test/resources/" + basename;
                new File(path).mkdirs();
                try (Closeable c = new FileOutputStream(path + "/" + file, true)) {
                }
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }*/

        TextMethodTester<GatewayTester> tester = new TextMethodTester<>(
                basename + "/in.yaml",
                VanillaGatewayTest::createGateway,
                GatewayTester.class,
                basename + "/out.yaml");
        tester.setup(basename + "/setup.yaml");
//                .timeoutMS(Jvm.isDebug() ? 2_000 : 200);
        DtoRegistry<SystemMessages> dtoRegistry = DtoRegistry.newRegistry(SystemMessages.class);
        SetTimeProvider stp = new SetTimeProvider("2018-09-03T16:00:00.000001")
                .autoIncrement(1, TimeUnit.MICROSECONDS);
        KeyPair kp = new KeyPair('X');
        Consumer<Object[]> signer = args -> {
            if (args.length > 0 && args[0] instanceof VanillaSignedMessage) {
                VanillaSignedMessage sm = (VanillaSignedMessage) args[0];
                if (!sm.signed()) {
                    sm.dtoRegistry(dtoRegistry);
                    sm.protocol(dtoRegistry.protocolFor(sm.getClass()));
                    sm.messageType(dtoRegistry.messageTypeFor(sm.getClass()));
                    sm.sign(kp.secretKey, stp);
                }
            }
        };
        tester.methodReaderInterceptor((m, o, args, invocation) -> {
            signer.accept(args);
            invocation.invoke(m, o, args);
        });
        tester.methodWriterListener((name, args) -> {
            signer.accept(args);
        });
        try {
            tester.run();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        assertEquals(tester.expected(), tester.actual());
    }

    static SystemMessages createGateway(GatewayTester tester) {
        DtoRegistry<SystemMessages> dtoRegistry = DtoRegistry.newRegistry(SystemMessages.class);
        KeyPair kp = new KeyPair(7);
        KeyPair kp2 = new KeyPair('X');
        SetTimeProvider stp = new SetTimeProvider("2018-08-20T12:53:05.000001")
                .autoIncrement(1, TimeUnit.MICROSECONDS);
        VanillaGateway gateway = VanillaGateway.newGateway(
                dtoRegistry,
                kp,
                "local",
                new long[]{kp.address(), kp2.address()},
                50,
                50,
                tester,
                tester,
                stp);
        gateway.tcpMessageListener(tester);
        return gateway;
    }

    @Test
    public void createAddressRequest() {
        test("gateway/createAddressRequest");
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

}
