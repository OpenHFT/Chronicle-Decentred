package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.annotation.UsedViaReflection;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.*;
import net.openhft.chronicle.decentred.remote.rpc.KeyPair;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.wire.TextMethodTester;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
        tester.setup(basename + "/setup.yaml")
                .timeoutMS(Jvm.isDebug() ? 2_000 : 200);
        try {
            tester.run();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        assertEquals(tester.expected(), tester.actual());
    }

    static SystemMessages createGateway(GatewayTester tester) {
        long address = DecentredUtil.parseAddress("server");
        DtoRegistry<SystemMessages> dtoRegistry = DtoRegistry.newRegistry(SystemMessages.class);
        VanillaGateway gateway = VanillaGateway.newGateway(
                dtoRegistry,
                address,
                "local",
                new long[]{address, DecentredUtil.parseAddress("phccofmpy6ci")},
                50,
                50,
                tester,
                tester);
        KeyPair kp = new KeyPair(17);
        SetTimeProvider stp = new SetTimeProvider("2018-08-20T12:53:05.000001")
                .autoIncrement(1, TimeUnit.MICROSECONDS);
        gateway.tcpMessageListener(tester);
        return new SystemMessages() {
            @Override
            public void createAddressRequest(CreateAddressRequest createAddressRequest) {
                sign(createAddressRequest);
                gateway.createAddressRequest(createAddressRequest);
            }

            @Override
            public void verificationEvent(VerificationEvent verificationEvent) {
                sign(verificationEvent);
                gateway.verificationEvent(verificationEvent);
            }

            @Override
            public void invalidationEvent(InvalidationEvent invalidationEvent) {
                sign(invalidationEvent);
                gateway.invalidationEvent(invalidationEvent);
            }

            @Override
            public void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent) {
                sign(transactionBlockEvent);
                gateway.transactionBlockEvent(transactionBlockEvent);
            }

            @Override
            public void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent) {
                sign(transactionBlockGossipEvent);
                gateway.transactionBlockGossipEvent(transactionBlockGossipEvent);
            }

            @Override
            public void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent) {
                sign(transactionBlockVoteEvent);
                gateway.transactionBlockVoteEvent(transactionBlockVoteEvent);
            }

            @Override
            public void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent) {
                sign(endOfRoundBlockEvent);
                gateway.endOfRoundBlockEvent(endOfRoundBlockEvent);
            }

            @UsedViaReflection
            public void processOneBlock() {
                gateway.processOneBlock();
            }

            private void sign(VanillaSignedMessage message) {
                message.protocol(dtoRegistry.protocolFor(message.getClass()))
                        .messageType(dtoRegistry.messageTypeFor(message.getClass()));
                message.sign(kp.secretKey, stp);
            }
        };
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
