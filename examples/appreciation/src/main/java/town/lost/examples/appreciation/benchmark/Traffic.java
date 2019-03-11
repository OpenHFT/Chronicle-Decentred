package town.lost.examples.appreciation.benchmark;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.server.RPCBuilder;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.salt.Ed25519;
import org.jetbrains.annotations.NotNull;
import town.lost.examples.appreciation.api.AppreciationMessages;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OpeningBalance;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Traffic  {
    private static final double START_AMOUNT = 2_000_000d;

    public static final long GIVER = 1;
    public static final long TAKER = 2;
    private static final long[] ACCOUNTS = {GIVER, TAKER};

    public Traffic() {}

    private static class Client {
        private final long accountSeed;
        private final long address;
        private final RPCClient<AppreciationMessages, AppreciationResponses> client;

        private Client(long accountSeed, long address, RPCClient<AppreciationMessages, AppreciationResponses> client) {
            this.accountSeed = accountSeed;
            this.address = address;
            this.client = client;
        }

        public void close() {
            client.close();
        }

        @NotNull
        public String toString() {
            return "Client(" + name() + ")";
        }

        @NotNull
        private String name() {
            return DecentredUtil.toAddressString(address);
        }

        private AppreciationMessages toDefault() {
            return client.toDefault();
        }

    }

    public static void main(String[] args) {
        final String[] addrPair = args[0].split(":");
        final InetSocketAddress socketAddress = InetSocketAddress.createUnresolved(addrPair[0], Integer.parseInt(addrPair[1]));
        System.out.println("Connecting to Gateway at " + socketAddress);

        List<Client> clients = Arrays.stream(ACCOUNTS).mapToObj(accountSeed -> {
            final BytesStore privateKey = DecentredUtil.testPrivateKey(accountSeed);
            final Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
            final Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
            Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);

            long address = DecentredUtil.toAddress(publicKey); // Isn't this the address to use?
            String name = DecentredUtil.toAddressString(address);
            System.out.println("Account " + accountSeed + " is " + name);

            System.out.println("Setting RPC client");
            RPCClient<AppreciationMessages, AppreciationResponses> rpcClient = RPCBuilder.of(17, AppreciationMessages.class, AppreciationResponses.class)
                .secretKey(secretKey)
                .publicKey(publicKey)
                .createClient(name, socketAddress, new Peer.ResponseSink());
            Client client = new Client(accountSeed, address, rpcClient);

            System.out.println("Waiting some time before sending first client message to " + name);
            Jvm.pause(7000);

            System.out.println("Sending CreateAddressRequest");

            client.toDefault().createAddressRequest(new CreateAddressRequest()
                .address(address)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .publicKey(publicKey)
            );
            return client;
        }).collect(Collectors.toList());

        clients.forEach(client -> {
            System.out.println("Waiting some time before sending second client message to " + client.name());
            Jvm.pause(7000);

            System.out.println("Setting account " + client + " to " + START_AMOUNT);

            final OpeningBalance ob = new OpeningBalance()
                .address(client.address)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .init(client.address, START_AMOUNT);
            client.toDefault().openingBalance(ob);
        });

        System.out.println("Waiting some time before sending give message...");
        Jvm.pause(15_000);

        final Give give = new Give()
            .address(clients.get(0).address)
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .init(clients.get(1).address, 17);
        clients.get(0).toDefault().give(give);

        System.out.println("Done.");
        clients.forEach(client -> {
            System.out.println("Waiting");
            Jvm.pause(2000);
            System.out.println("Closing " + client.name());
            client.close();

        });
        System.out.println("Done.");
        Jvm.pause(7000);

    }


}