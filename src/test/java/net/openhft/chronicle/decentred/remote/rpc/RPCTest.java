package net.openhft.chronicle.decentred.remote.rpc;

import net.openhft.chronicle.core.Mocker;
import net.openhft.chronicle.decentred.api.Verifier;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.server.VanillaVerifyIP;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RPCTest {
    @Test
    public void testVerify() throws IOException, InterruptedException {
        KeyPair zero = new KeyPair(0);
        KeyPair one = new KeyPair(1);

        DtoRegistry<Verifier> protocol = DtoRegistry.newRegistry(Verifier.class)
                .addProtocol(1, Verifier.class);
        RPCServer<Verifier> server = new RPCServer<>("test",
                9999,
                9999,
                zero.publicKey,
                zero.secretKey,
                Verifier.class,
                protocol,
                VanillaVerifyIP::new);

        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        Verifier verifier = Mocker.queuing(Verifier.class, "", queue);
        RPCClient<Verifier> client = new RPCClient<>(
                "test",
                "localhost",
                9999,
                zero.secretKey,
                protocol,
                verifier);

        VerificationEvent message = protocol.create(VerificationEvent.class);
        message.keyVerified(one.publicKey);
        client.write(message);
        while (queue.size() < 1)
            Thread.sleep(100);
        for (String s : queue) {
            System.out.println(s);
        }
        client.close();
        server.close();
    }

}
