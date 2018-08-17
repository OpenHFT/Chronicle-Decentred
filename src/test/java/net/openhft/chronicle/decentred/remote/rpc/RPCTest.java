package net.openhft.chronicle.decentred.remote.rpc;

import net.openhft.chronicle.core.Mocker;
import net.openhft.chronicle.decentred.api.Verifier;
import net.openhft.chronicle.decentred.dto.Verification;
import net.openhft.chronicle.decentred.util.DtoParserBuilder;
import net.openhft.chronicle.decentred.verification.VanillaVerifyIP;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RPCTest {
    @Ignore("TODO FIX")
    @Test
    public void testVerify() throws IOException, InterruptedException {
        KeySet zero = new KeySet(0);
        KeySet one = new KeySet(1);

        DtoParserBuilder<Verifier> protocol = new DtoParserBuilder<Verifier>()
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
        RPCClient<Verifier, Verifier> client = new RPCClient<>(
                "test",
                "localhost",
                9999,
                zero.secretKey,
                Verifier.class,
                protocol.get(),
                verifier);

        Verification message = protocol.create(Verification.class);
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
