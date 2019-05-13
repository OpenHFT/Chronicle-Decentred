package town.lost.examples.appreciation.benchmark;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.server.RPCBuilder;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.KeyPair;
import org.jetbrains.annotations.NotNull;
import town.lost.examples.appreciation.api.AppreciationMessages;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OpeningBalance;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public final class Traffic  {
    private static final double START_AMOUNT = 2_000_000d;

    private static final long GIVER = 1;
    private static final long TAKER = 2;
    private static final long[] ACCOUNTS = {GIVER, TAKER};

    public Traffic() {}

    private enum BencmarkState {
        WARMUP, RUN;
    }

    private final static class Client {
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
            final KeyPair kp = new KeyPair(accountSeed);
            final BytesStore publicKey = kp.publicKey;

            final long address = DecentredUtil.toAddress(publicKey); // Isn't this the address to use?
            final String name = DecentredUtil.toAddressString(address);
            System.out.println("Account " + accountSeed + " is " + name);

            System.out.println("Setting RPC client");
            final RPCClient<AppreciationMessages, AppreciationResponses> rpcClient = RPCBuilder.of(17, AppreciationMessages.class, AppreciationResponses.class)
                .keyPair(kp)
                .createClient(name, socketAddress, new Peer.ResponseSink());

            final Client client = new Client(accountSeed, address, rpcClient);

/*            System.out.println("Waiting some time before sending first client message to " + name);
            Jvm.pause(1000);*/

            System.out.println("Sending CreateAddressRequest");

            client.toDefault().createAddressRequest(new CreateAddressRequest()
                .address(address)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .publicKey(publicKey)
            );
            return client;
        }).collect(toList());

        clients.forEach(client -> {
            System.out.println("Waiting some time before sending second client message to " + client.name());
            Jvm.pause(1000);

            System.out.println("Setting account " + client + " to " + START_AMOUNT);

            final OpeningBalance ob = new OpeningBalance()
                .address(client.address)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .init(client.address, START_AMOUNT);
            client.toDefault().openingBalance(ob);
        });

        System.out.println("Waiting some time before sending give message...");
        Jvm.pause(1_000);

        final Give give = new Give()
            .address(clients.get(0).address)
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .init(clients.get(1).address, 1);
        clients.get(0).toDefault().give(give);


        System.out.println("Waiting some time before sending a second give message...");
        Jvm.pause(1_000);
        final Give give2 = new Give()
            .address(clients.get(0).address)
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .init(clients.get(1).address, 1);
        clients.get(0).toDefault().give(give2);



        System.out.println("Done.");
        Jvm.pause(2000);

        for (BencmarkState state : BencmarkState.values()) {

            System.out.println("Benchmark: " + state);

            final int iterations = 30_000;

            System.out.println("Preparing Give messages....");
            final List<Give> gives = IntStream.range(0, iterations)
                .mapToObj(i ->
                    new Give()
                        .address(clients.get(0).address)
                        .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                        .init(clients.get(1).address, 1)
                )
                .collect(toList());


            final long start = System.nanoTime();
            System.out.println("Running benchmark...");
            for (int i = 0; i < iterations; i++) {
                clients.get(0).toDefault().give(gives.get(i));
            }
            final long duration = System.nanoTime() - start;
            final double ratio = ((double) iterations) * 1e9 / (double) duration;
            System.out.format("Completed %,d iterations in %,d ms (%,.0f TPS)%n", iterations, duration / 1000000, ratio);

            Jvm.pause(500);
        }

        clients.forEach(client -> {
            System.out.println("Closing " + client.name());
            client.close();

        });

        System.out.println("Done.");

    }


}