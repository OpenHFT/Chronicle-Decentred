package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.Mocker;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.internal.server.VanillaBlockEngine;
import net.openhft.chronicle.decentred.internal.server.VanillaTransactionProcessor;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;

final class RPCGatewayTest {

    @Test
    void endToEnd() throws IOException, InterruptedException {
        // A totally unscientific number hinting at the fact that this test fails if the engine is designed to move slowly
        final int extraWaitTime = VanillaBlockEngine.STEP_PAUSE_MILLIS / 10;

        assertTimeout(ofSeconds(5 + extraWaitTime), () -> {
                try {
                    KeyPair kp = new KeyPair(7);
                    RPCBuilder<SystemMessages, SystemMessages> rpcBuilder = RPCBuilder.of(SystemMessages.class, SystemMessages.class)
                        .addClusterAddress(DecentredUtil.toAddress(kp.publicKey))
                        .keyPair(kp);
                    VanillaTransactionProcessor vtp = new VanillaTransactionProcessor();
                    try (RPCServer<SystemMessages, SystemMessages> server = rpcBuilder.createServer(9009, vtp, vtp, config -> VanillaGateway.newGateway(
                        config.dtoRegistry(),
                        config.keyPair(),
                        config.regionStr(),
                        config.clusterAddresses(),
                        config.mainPeriodMS(),
                        config.localPeriodMS(),
                        vtp,
                        vtp,
                        config.timeProvider()))) {
                        System.out.println("Server address " + DecentredUtil.toAddressString(DecentredUtil.toAddress(kp.publicKey)));

                        KeyPair kp2 = new KeyPair(17);
                        DtoRegistry<SystemMessages> dtoRegistry = DtoRegistry.newRegistry(SystemMessages.class);
                        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
                        SystemMessages listener = Mocker.queuing(SystemMessages.class, "", queue);
                        try (RPCClient<SystemMessages, SystemMessages> client = new RPCClient<>("test", "localhost", 9009, kp2.secretKey, dtoRegistry, listener, SystemMessages.class)) {
                            System.out.println("Client address " + DecentredUtil.toAddressString(DecentredUtil.toAddress(kp2.publicKey)));
                            client.toDefault().createAddressRequest(new CreateAddressRequest().address(42));
                            String s = queue.poll((Jvm.isDebug() ? 100 : 10) + extraWaitTime, TimeUnit.SECONDS);
                            assertEquals("createAddressEvent[!CreateAddressEvent {\n" +
                                "  timestampUS: {deleted},\n" +
                                "  address: nphccofmpy6ci,\n" +
                                "  createAddressRequest: {\n" +
                                "    timestampUS: {deleted},\n" +
                                "    address: nud6jbceicts2,\n" +
                                "    publicKey: !!binary TsXED8x8VoxtLgRu7iPaz4aAhfQUtmvee9KRyhDKk+o=\n" +
                                "  }\n" +
                                "}\n" +
                                "]", s.replaceAll("timestampUS: 20[^,]+", "timestampUS: {deleted}"));
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    fail(e);
                }
            }
        );
    }

}
