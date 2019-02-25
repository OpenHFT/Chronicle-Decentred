package town.lost.examples.appreciation.benchmark;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.api.BlockchainPhase;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.TransactionProcessor;
import net.openhft.chronicle.decentred.dto.*;
import net.openhft.chronicle.decentred.remote.net.TCPConnection;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.server.BlockEngine;
import net.openhft.chronicle.decentred.server.GatewayConfiguration;
import net.openhft.chronicle.decentred.server.VanillaBlockEngine;
import net.openhft.chronicle.decentred.server.VanillaGateway;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.salt.Ed25519;
import town.lost.examples.appreciation.VanillaAppreciationGateway;
import town.lost.examples.appreciation.VanillaAppreciationTransactions;
import town.lost.examples.appreciation.api.AppreciationMessages;
import town.lost.examples.appreciation.api.AppreciationRequests;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.api.AppreciationTransactions;
import town.lost.examples.appreciation.dto.*;
import town.lost.examples.appreciation.util.BalanceStore;
import town.lost.examples.appreciation.util.VanillaBalanceStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Peer extends Node<AppreciationMessages, AppreciationRequests> {
    private static final double START_AMOUNT = 2_000_000d;
    public static final String GIVER = "giver";
    public static final String TAKER = "taker";
    private static final String[] ACCOUNTS = {GIVER, TAKER};

    private RPCServer<AppreciationMessages, AppreciationRequests> rpcServer;
    private final InetSocketAddress socketAddress;
    private final BalanceStore balanceStore;

    public Peer(long seed, InetSocketAddress socketAddress, BalanceStore balanceStore) {
        super(seed, AppreciationMessages.class, AppreciationRequests.class);

        this.socketAddress = socketAddress;
        this.balanceStore = balanceStore;
    }

    public void start() throws IOException {
        MessageRouter<AppreciationResponses> messageRouter = address -> new AppreciationResponses() {
            @Override
            public void onBalance(OnBalance onBalance) {
                System.out.println("error onBalance = " + onBalance);
            }

            @Override
            public void verificationEvent(VerificationEvent verificationEvent) {
                System.out.println("error verificationEvent = " + verificationEvent);
            }

            @Override
            public void invalidationEvent(InvalidationEvent invalidationEvent) {
                System.out.println("error invalidationEvent = " + invalidationEvent);
            }
        };

        AppreciationRequests mainProcessor = new Processor(messageRouter, balanceStore);
        AppreciationRequests localProcessor = new Processor(messageRouter, balanceStore);

        Function<GatewayConfiguration<AppreciationMessages>, VanillaGateway> gatewayConstructor = config -> {
            long region = DecentredUtil.parseAddress(config.regionStr());
            BlockEngine mainEngine = VanillaBlockEngine.newMain(config.dtoRegistry(), config.address(),
                config.mainPeriodMS(), config.clusterAddresses(), mainProcessor);
            BlockEngine localEngine = VanillaBlockEngine.newLocal(config.dtoRegistry(), config.address(), region,
                config.localPeriodMS(), config.clusterAddresses(), localProcessor);

            AppreciationTransactions blockChain = new AppreciationTransactions() {
                @Override
                public void openingBalance(OpeningBalance openingBalance) {
                    localEngine.onMessage(openingBalance);
                }

                @Override
                public void give(Give give) {
                    localEngine.onMessage(give);
                }

                @Override
                public void topup(Topup topup) {
                    localEngine.onMessage(topup);
                }
            };

            return new VanillaAppreciationGateway(
                region, mainEngine, localEngine, messageRouter, blockChain, balanceStore);
        };
        rpcServer = getRpcBuilder(). createServer(socketAddress.getHostName(), socketAddress.getPort(), mainProcessor, localProcessor, gatewayConstructor);
        ((TransactionProcessor) mainProcessor).messageRouter(rpcServer);
        ((TransactionProcessor) localProcessor).messageRouter(rpcServer);
    }


    @Override
    public void close() {
        rpcServer.close();
    }

    public int getPort() {
        return rpcServer.getPort();
    }

    private void connect(long address, InetSocketAddress serverAddress) {
        RPCClient<AppreciationMessages, AppreciationRequests> client = getRpcBuilder()
            .createClient("to " + serverAddress, serverAddress, new IncomingProcessor());
        rpcServer.setRoute(address, client);
    }


    private void setOpeningBalances() throws InterruptedException {
        //Thread.sleep(10000);
/*

        Map<String, Bytes> publicKeys = new HashMap<>();
        Map<String, Bytes> secretKeys = new HashMap<>();

        Stream.of(GIVER*/
/*ACCOUNTS*//*
).forEachOrdered(accountName -> {
            long address = DecentredUtil.parseAddress(accountName);
            BytesStore privateKey = DecentredUtil.testPrivateKey(address);
            Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
            Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
            Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);

            //long address = DecentredUtil.toAddress(publicKey); // Isn't this the address to use?

            publicKeys.put(accountName, publicKey);
            secretKeys.put(accountName, secretKey);

            RPCClient<AppreciationMessages, AppreciationResponses> client = getRpcBuilder()
                .createAccountClient(accountName, secretKey, socketAddress, new ResponseSink());

            Jvm.pause(7000);
            System.out.println("CreateAddressRequest");

            client.write(new CreateAddressRequest()
                .address(address)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .publicKey(publicKey)
            );

            Jvm.pause(7000);

            System.out.println("Setting account " + accountName + " to " + START_AMOUNT);

            OpeningBalance ob = new OpeningBalance()
                .address(address)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .init(address, START_AMOUNT);
            client.write(ob);  // when signing this, the address is garbled!

            System.out.println("Waiting");
            Jvm.pause(2000);
             System.out.println("Closing");
            client.close();

        });

*/
        /*

        Thread.sleep(10000);


        RPCClient<AppreciationMessages, AppreciationRequests> client = getRpcBuilder()
            .createAccountClient(GIVER, secretKeys.get(GIVER), socketAddress, new ResponseSink());

                client.write(new Give()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .address(DecentredUtil.parseAddress(GIVER))
            .init(DecentredUtil.parseAddress(TAKER), 10));

        client.close();*/
    }

    private class IncomingProcessor implements AppreciationRequests {

        @Override
        public void queryBalance(QueryBalance queryBalance) {
            System.out.println("IncomingProcessor.queryBalance");
        }

        @Override
        public void createAddressRequest(CreateAddressRequest createAddressRequest) {
            System.out.println("IncomingProcessor.createAddressRequest");
        }

        @Override
        public void openingBalance(OpeningBalance openingBalance) {
            System.out.println("IncomingProcessor.openingBalance");
        }

        @Override
        public void give(Give give) {
            System.out.println("IncomingProcessor.give");
        }

        @Override
        public void topup(Topup topup) {
            System.out.println("IncomingProcessor.topup");
        }
    }

    private class Processor extends VanillaAppreciationTransactions implements AppreciationRequests, TransactionProcessor {
        private BlockchainPhase blockchainPhase;

        public Processor(MessageRouter<AppreciationResponses> router, BalanceStore balanceStore) {
            super(router, balanceStore);
        }

        @Override
        public void queryBalance(QueryBalance queryBalance) {

        }

        @Override
        public void createAddressRequest(CreateAddressRequest createAddressRequest) {
            router.to(createAddressRequest.address())
                .createAddressEvent(new CreateAddressEvent()
                    .createAddressRequest(createAddressRequest));

            balanceStore.setBalance(createAddressRequest.address(), 0.0); // Sets an initial value, indicating that the account is created

        }

        @Override
        public void blockchainPhase(BlockchainPhase blockchainPhase) {
            this.blockchainPhase = blockchainPhase;
        }

        @Override
        public void messageRouter(MessageRouter messageRouter) {
            this.router = messageRouter;
        }
    }



    public static class ResponseSink implements AppreciationResponses {
        @Override
        public void onBalance(OnBalance onBalance) {
            System.out.println("RequestsSink.onBalance = " + onBalance);
        }

        @Override
        public void verificationEvent(VerificationEvent verificationEvent) {
            System.out.println("RequestsSink.verificationEvent = " + verificationEvent);
        }

        @Override
        public void invalidationEvent(InvalidationEvent invalidationEvent) {
            System.out.println("RequestsSink.invalidationEvent = " + invalidationEvent);
        }

        @Override
        public void applicationError(ApplicationErrorResponse applicationErrorResponse) {
            System.out.println("RequestsSink.applicationErrorResponse = " + applicationErrorResponse);
        }

        @Override
        public void createAddressEvent(CreateAddressEvent createAddressEvent) {
            System.out.println("RequestsSink.createAddressEvent = " + createAddressEvent);
        }

        @Override
        public void onConnection(TCPConnection connection) {
            System.out.println("RequestsSink.connection = " + connection);
        }

        @Override
        public void onDisconnection(TCPConnection connection) {
            System.out.println("RequestsSink.connection = " + connection);
        }

        /*@Override
        public void queryBalance(QueryBalance queryBalance) {
            System.out.println("ResponseSink.queryBalance");
        }

        @Override
        public void createAddressRequest(CreateAddressRequest createAddressRequest) {
            System.out.println("ResponseSink.createAddressRequest");
        }

        @Override
        public void openingBalance(OpeningBalance openingBalance) {
            System.out.println("ResponseSink.openingBalance");
        }

        @Override
        public void give(Give give) {
            System.out.println("ResponseSink.give");
        }

        @Override
        public void topup(Topup topup) {
            System.out.println("ResponseSink.topup");
        }*/
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int peerSeedOffset = 1000;
        IntUnaryOperator seedForPeerIdx = i -> i + peerSeedOffset;

        BalanceStore balanceStore = new VanillaBalanceStore();

        final int myAddressIndex = Integer.parseInt(args[0]);

        List<InetSocketAddress> socketAddresses = Arrays.stream(args[1].split(","))
            .map(addrString -> {
                String[] addrPair = addrString.split(":");
                return InetSocketAddress.createUnresolved(addrPair[0], Integer.parseInt(addrPair[1]));
            })
            .collect(toList());

        List<Long> addresses = IntStream.range(0, socketAddresses.size())
            .map(seedForPeerIdx)
            .mapToObj(Node::addressFromSeed)
            .collect(toList());

        IntStream.range(0, addresses.size())
            .forEachOrdered(i -> System.out.format("Peer #%d, address: %21d, addressString: %32s at %s %s%n",
                i,
                addresses.get(i),
                DecentredUtil.toAddressString(addresses.get(i)),
                socketAddresses.get(i),
                i == myAddressIndex?"(*)":""
                )
            );


        final InetSocketAddress myAddress = socketAddresses.get(myAddressIndex);

        final Peer peer = new Peer(seedForPeerIdx.applyAsInt(myAddressIndex), myAddress, balanceStore);

        IntStream.range(0, addresses.size())
            //.filter(i -> i != myAddressIndex)  // Exclude my own address
            .mapToLong(addresses::get)
            .forEachOrdered(peer::addClusterAddress);

        peer.start();

        IntStream.range(0, socketAddresses.size())
            .filter(i -> i != myAddressIndex)
            .peek(i -> System.out.format("Connecting to peer #%d, address %21d at %s%n", i, addresses.get(i), socketAddresses.get(i)))
            .forEachOrdered(i -> peer.connect(addresses.get(i), socketAddresses.get(i)));

        System.out.format("Peer #%d started at %s%n", myAddressIndex, myAddress);

        if (myAddressIndex == 0 && false) {
            System.out.println("Waiting for network");
            Jvm.pause(5000);
            System.out.println("Send opening balances");
            peer.setOpeningBalances();
        }

        while (System.in.available() == 0) {
            Thread.sleep(5000);
            System.out.println("balanceStore = " + balanceStore);
        }

        // TODO - currently not used

        peer.close();
        System.exit(0);

    }
}