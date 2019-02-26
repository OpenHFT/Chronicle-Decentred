package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.Mocker;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.base.DtoAliases;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class DecentredClientServerTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    public void test() throws IOException {
        KeyPair kp = new KeyPair(7);
        StringWriter out = new StringWriter();
        SystemMessages logging = Mocker.logging(SystemMessages.class, "", out);
        DtoRegistry<SystemMessages> dtoRegistry = DtoRegistry.newRegistry(SystemMessages.class);

        RPCServer<SystemMessages, SystemMessages> server = new RPCServer<>(
                "test", 9900,
                1,
                kp.publicKey, kp.secretKey,
                SystemMessages.class,
                dtoRegistry,
                s -> logging);

        server.register(2, kp.publicKey);

        List<InetSocketAddress> addresses = Arrays.asList(new InetSocketAddress("localhost", 9900));

        SystemMessages logging2 = Mocker.logging(SystemMessages.class, "", out);
        RPCClient<SystemMessages, SystemMessages> client = new RPCClient<>(
                "test-client", addresses, kp.secretKey, dtoRegistry, logging2, SystemMessages.class)
                .timeProvider(new SetTimeProvider("2018-08-25T09:45:04.18"));

        CreateAddressRequest cnac = new CreateAddressRequest()
                .publicKey(kp.publicKey);

        client.toDefault().createAddressRequest(cnac);
        System.out.println(cnac);

        for (int i = 0; i <= 20; i++) {
            assertTrue(i < 20);
            Jvm.pause(Jvm.isDebug() ? 2000 : 25);
            if (out.toString().contains("createAddressRequest")) {
                break;
            }
            System.out.println(out);
        }
        assertEquals(
                "createAddressRequest[!CreateAddressRequest {\n" +
                        "  timestampUS: 2018-08-25T09:45:04.18,\n" +
                        "  address: phccofmpy6ci,\n" +
                        "  publicKey: !!binary 9M9t8hyt2kEJmL46Fs+si0VigLTMQt9OafgMm3ljIOg=\n" +
                        "}\n" +
                        "]\n",
                out.toString().replaceAll("\r", ""));

        client.close();
        server.close();
    }
}
