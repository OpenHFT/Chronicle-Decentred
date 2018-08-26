package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.Mocker;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.CreateAddressRequest;
import net.openhft.chronicle.decentred.remote.rpc.KeyPair;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class RPCGateway {
    @Test
    public void endToEnd() throws IOException, InterruptedException {
        KeyPair kp = new KeyPair(7);
        RPCBuilder<SystemMessages> rpcBuilder = RPCBuilder.of(SystemMessages.class)
                .addClusterAddress(DecentredUtil.toAddress(kp.publicKey))
                .secretKey(kp.secretKey)
                .publicKey(kp.publicKey);
        VanillaTransactionProcessor vtp = new VanillaTransactionProcessor();
        RPCServer<SystemMessages> server = rpcBuilder.createServer(9009, vtp, vtp);
        System.out.println("Server address " + DecentredUtil.toAddressString(DecentredUtil.toAddress(kp.publicKey)));

        KeyPair kp2 = new KeyPair(17);
        DtoRegistry<SystemMessages> dtoRegistry = DtoRegistry.newRegistry(SystemMessages.class);
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        SystemMessages listener = Mocker.queuing(SystemMessages.class, "", queue);
        RPCClient<SystemMessages> client = new RPCClient<>("test", "localhost", 9009, kp2.secretKey, dtoRegistry, listener);
        System.out.println("Client address " + DecentredUtil.toAddressString(DecentredUtil.toAddress(kp2.publicKey)));
        client.toDefault().createAddressRequest(new CreateAddressRequest());
        String s = queue.poll(Jvm.isDebug() ? 100 : 10, TimeUnit.SECONDS);
        assertEquals("createAddressEvent[!CreateAddressEvent {\n" +
                "  timestampUS: {deleted},\n" +
                "  address: phccofmpy6ci,\n" +
                "  createAddressRequest: {\n" +
                "    timestampUS: {deleted},\n" +
                "    address: ud6jbceicts2,\n" +
                "    publicKey: !!binary TsXED8x8VoxtLgRu7iPaz4aAhfQUtmvee9KRyhDKk+o=\n" +
                "  }\n" +
                "}\n" +
                "]", s.replaceAll("timestampUS: 20[^,]+", "timestampUS: {deleted}"));
        server.close();
    }
}
