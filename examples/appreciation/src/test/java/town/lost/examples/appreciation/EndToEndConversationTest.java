package town.lost.examples.appreciation;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.VanillaBytes;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.api.BlockchainPhase;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.TransactionProcessor;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.address.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.server.*;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.salt.Ed25519;
import town.lost.examples.appreciation.api.*;
import town.lost.examples.appreciation.dto.*;
import town.lost.examples.appreciation.util.BalanceStore;
import town.lost.examples.appreciation.util.Balances;
import town.lost.examples.appreciation.util.VanillaBalanceStore;

import java.io.IOException;
import java.util.function.Function;

public class EndToEndConversationTest {

    private abstract static class Node<U extends T, T> {
        private final BytesStore privateKey;
        private final VanillaBytes<Void> publicKey;
        private final VanillaBytes<Void> secretKey;
        private final RPCBuilder<U, T> rpcBuilder;
        private AppreciationGateway gateway;

        private Node(int seed, Class<U> uClass, Class<T> tClass) {
            privateKey = DecentredUtil.testPrivateKey(seed);
            publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
            secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
            Ed25519.privateToPublicAndSecret(this.publicKey, this.secretKey, this.privateKey);

            rpcBuilder = RPCBuilder.of(17, uClass, tClass)
                .addClusterAddress(DecentredUtil.toAddress(this.publicKey))
                .secretKey(this.secretKey)
                .publicKey(this.publicKey);

        }

        public long address() {
            return DecentredUtil.toAddress(publicKey);
        }

        public BytesStore getPrivateKey() {
            return privateKey;
        }

        public VanillaBytes<Void> getPublicKey() {
            return publicKey;
        }

        public VanillaBytes<Void> getSecretKey() {
            return secretKey;
        }

        public RPCBuilder<U, T> getRpcBuilder() {
            return rpcBuilder;
        }

    }

    static class TransactionsImpl extends VanillaAppreciationTransactions implements AppreciationRequests, TransactionProcessor {
        private BlockchainPhase blockchainPhase;

        public TransactionsImpl(MessageRouter<AppreciationResponses> router, BalanceStore balanceStore) {
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

    private static class ServerNode extends Node<AppreciationMessages, AppreciationRequests> {
        private final RPCServer<AppreciationMessages, AppreciationRequests> rpcServer;

        private ServerNode(int seed, int port, BalanceStore balanceStore) throws IOException {
            super(seed, AppreciationMessages.class, AppreciationRequests.class);

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

            AppreciationRequests mainProcessor = new TransactionsImpl(messageRouter, balanceStore);
            AppreciationRequests localProcessor = new TransactionsImpl(messageRouter, balanceStore);

            Function<GatewayConfiguration<AppreciationMessages>, VanillaGateway> gatewayConstructor = config -> {
                long region = DecentredUtil.parseAddress(config.regionStr());
                Bytes secretKey = getRpcBuilder().secretKey();
                BlockEngine mainEngine = VanillaBlockEngine.newMain(config.dtoRegistry(), config.address(),
                    config.mainPeriodMS(), config.clusterAddresses(), mainProcessor, secretKey);
                BlockEngine localEngine = VanillaBlockEngine.newLocal(config.dtoRegistry(), config.address(), region,
                    config.localPeriodMS(), config.clusterAddresses(), localProcessor, secretKey);

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
            rpcServer = getRpcBuilder().createServer(port, mainProcessor, localProcessor, gatewayConstructor);
            ((TransactionProcessor) mainProcessor).messageRouter(rpcServer);
            ((TransactionProcessor) localProcessor).messageRouter(rpcServer);
        }

    }

    private static class ClientNode extends Node<AppreciationMessages, AppreciationResponses> implements AppreciationResponses {
        private final RPCClient<AppreciationMessages, AppreciationResponses> rpcClient;

        private ClientNode(int seed, int serverPort) {
            super(seed, AppreciationMessages.class, AppreciationResponses.class);
            DtoRegistry<AppreciationMessages> dtoRegistry = DtoRegistry.newRegistry(17, AppreciationMessages.class);
            rpcClient = new RPCClient<>("test", "0.0.0.0", serverPort, getSecretKey(), dtoRegistry, this, AppreciationResponses.class);
        }

        public void connect() {
            sendMsg(new CreateAddressRequest()
                .address(0)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .publicKey(getPublicKey()));
        }

        public void sendMsg(VanillaSignedMessage msg) {
            rpcClient.write(msg);
        }

        @Override
        public void onBalance(OnBalance onBalance) {
            System.out.println(address() + " onBalance = " + onBalance);
        }

        @Override
        public void verificationEvent(VerificationEvent verificationEvent) {
            System.out.println(address() + " verificationEvent = " + verificationEvent);
        }

        @Override
        public void invalidationEvent(InvalidationEvent invalidationEvent) {
            System.out.println(address() + " invalidationEvent = " + invalidationEvent);
        }
    }

    private static String formatBalance(BalanceStore store, long address) {
        Balances balance = store.getBalances(address);
        if (balance != null) {
            return "balance " + balance.balance();
        } else {
            return "<no balance>";
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        int serverPort = 9009;
        BalanceStore balanceStore = new VanillaBalanceStore();

        ServerNode server = new ServerNode(17, serverPort, balanceStore);

        ClientNode c1 = new ClientNode(42, serverPort);
        ClientNode c2 = new ClientNode(7, serverPort);

        c1.connect();
        c2.connect();

        //Thread.sleep(5000);

        OpeningBalance openingBalance1 = new OpeningBalance()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .init(c1.address(), 100);
        c1.sendMsg(openingBalance1);

        System.out.println("Sent one");
        System.out.println("c1 = " + formatBalance(balanceStore, c1.address()));
        System.out.println("c2 = " + formatBalance(balanceStore, c2.address()));
        Thread.sleep(2000);

        OpeningBalance openingBalance2 = new OpeningBalance()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .init(c2.address(), 20);
        c2.sendMsg(openingBalance2);

        System.out.println("Sent two");
        System.out.println("c1 = " + formatBalance(balanceStore, c1.address()));
        System.out.println("c2 = " + formatBalance(balanceStore, c2.address()));
        Thread.sleep(2000);

        c1.sendMsg(new Give()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .address(c1.address())
            .init(c2.address(), 7));

        c2.sendMsg(new Give()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .address(c2.address())
            .init(c1.address(), 3));


        System.out.println("c1 = " + formatBalance(balanceStore, c1.address()));
        System.out.println("c2 = " + formatBalance(balanceStore, c2.address()));
        Thread.sleep(3000);
        System.out.println("done");
        System.out.println("c1 = " + formatBalance(balanceStore, c1.address()));
        System.out.println("c2 = " + formatBalance(balanceStore, c2.address()));


    }
}
