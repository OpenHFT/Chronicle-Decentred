package town.lost.examples.appreciation;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.server.RPCBuilder;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.KeyPair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import town.lost.examples.appreciation.api.AppreciationMessages;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.benchmark.Node;
import town.lost.examples.appreciation.benchmark.Peer;
import town.lost.examples.appreciation.dto.OpeningBalance;
import town.lost.examples.appreciation.util.BalanceStore;
import town.lost.examples.appreciation.util.VanillaBalanceStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class ThreePeersConnectionTest {
    private static class Client {
        private final long address;
        private final RPCClient<AppreciationMessages, AppreciationResponses> client;

        private Client(long address, RPCClient<AppreciationMessages, AppreciationResponses> client) {
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


    private static Peer startPeer(List<InetSocketAddress> socketAddresses, int myAddressIndex) {
        int peerSeedOffset = 1000;
        IntUnaryOperator seedForPeerIdx = i -> i + peerSeedOffset;

        BalanceStore balanceStore = new VanillaBalanceStore();

        List<Long> addresses = IntStream.range(0, socketAddresses.size())
            .map(seedForPeerIdx)
            .mapToObj(Node::addressFromSeed)
            .collect(toList());


        final InetSocketAddress myAddress = socketAddresses.get(myAddressIndex);

        final Peer peer = new Peer(seedForPeerIdx.applyAsInt(myAddressIndex), myAddress, balanceStore);

        addresses.stream()
            .filter(i -> i != myAddressIndex)
            .forEachOrdered(peer::addClusterAddress);

        try {
            peer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IntStream.range(0, socketAddresses.size())
            .filter(i -> i != myAddressIndex)
            .peek(i -> System.out.format("Connecting to peer #%d, address %21d at %s%n", i, addresses.get(i), socketAddresses.get(i)))
            .forEachOrdered(i -> peer.connect(addresses.get(i), socketAddresses.get(i)));

        System.out.format("Peer #%d started at %s%n", myAddressIndex, myAddress);

        return peer;
    }


    @Test
    public void testFull() {
        List<InetSocketAddress> addresses = IntStream.range(9090, 9093).mapToObj(InetSocketAddress::new).collect(toList());

        List<Peer> peers = IntStream.range(0, addresses.size()).mapToObj(index -> startPeer(addresses, index)).collect(toList());

        long[] seeds = {7, 17};
        List<Client> clients = Arrays.stream(seeds).mapToObj(accountSeed -> {
            KeyPair kp = new KeyPair(accountSeed);
            final BytesStore publicKey = kp.publicKey;

            long address = DecentredUtil.toAddress(publicKey); // Isn't this the address to use?
            String name = DecentredUtil.toAddressString(address);
            RPCClient<AppreciationMessages, AppreciationResponses> rpcClient = RPCBuilder.of(17, AppreciationMessages.class, AppreciationResponses.class)
                .keyPair(kp)
                .createClient(name, addresses.get(0), new Peer.ResponseSink());
            Client client = new Client(address, rpcClient);

            client.toDefault().createAddressRequest(new CreateAddressRequest()
                .address(address)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .publicKey(publicKey)
            );
            return client;
        }).collect(Collectors.toList());


        double startAmount = 1000;

        clients.forEach(client -> {
            final OpeningBalance ob = new OpeningBalance()
                .address(client.address)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .init(client.address, startAmount);
            client.toDefault().openingBalance(ob);
        });

        Jvm.pause(2_000);  // During this pause, there are marshalling exceptions

        peers.forEach(Peer::close);

    }
}
