package town.lost.examples.appreciation.benchmark;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.api.BlockchainPhase;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.TransactionProcessor;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.address.InvalidationEvent;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.server.BlockEngine;
import net.openhft.chronicle.decentred.server.GatewayConfiguration;
import net.openhft.chronicle.decentred.internal.server.VanillaBlockEngine;
import net.openhft.chronicle.decentred.server.VanillaGateway;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import town.lost.examples.appreciation.VanillaAppreciationGateway;
import town.lost.examples.appreciation.VanillaAppreciationTransactions;
import town.lost.examples.appreciation.api.AppreciationMessages;
import town.lost.examples.appreciation.api.AppreciationRequests;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.api.AppreciationTransactions;
import town.lost.examples.appreciation.dto.*;
import town.lost.examples.appreciation.util.BalanceStore;
import town.lost.examples.appreciation.util.Balances;
import town.lost.examples.appreciation.util.VanillaBalanceStore;

import java.io.IOException;
import java.util.function.Function;

public class Server extends Node<AppreciationMessages, AppreciationRequests> {
    public static final int DEFAULT_SERVER_PORT = 9010;
    private static final double START_AMOUNT = 2_000_000d;
    private static final int MAX_SEED = 4;

    private final RPCServer<AppreciationMessages, AppreciationRequests> rpcServer;
    private TimeProvider timeProvider = UniqueMicroTimeProvider.INSTANCE;


    public Server(long seed, int port, BalanceStore balanceStore) throws IOException {
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
            BlockEngine mainEngine = BlockEngine.newMain(config.dtoRegistry(), config.keyPair(),
                config.mainPeriodMS(), config.clusterAddresses(), mainProcessor, timeProvider);
            BlockEngine localEngine = BlockEngine.newLocal(config.dtoRegistry(), config.keyPair(), region,
                config.localPeriodMS(), config.clusterAddresses(), localProcessor, timeProvider);

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
                config.keyPair().address(), mainEngine, localEngine, messageRouter, blockChain, balanceStore);
        };
        rpcServer = getRpcBuilder().createServer(port, mainProcessor, localProcessor, gatewayConstructor);
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

    private static String formatBalance(BalanceStore store, long address) {
        Balances balance = store.getBalances(address);
        if (balance != null) {
            return "balance " + balance.balance();
        } else {
            return "<no balance>";
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

    public static void main(String[] args) throws IOException, InterruptedException {
        BalanceStore balanceStore = new VanillaBalanceStore();
        Server server = new Server(42, DEFAULT_SERVER_PORT, balanceStore);

        System.out.println("Server started at port " + DEFAULT_SERVER_PORT);

        setOpeningBalances("0.0.0.0");

        System.out.println("Press <enter> to stop server.");

        while (System.in.available() == 0) {
            Thread.sleep(5000);
            System.out.println("balanceStore = " + balanceStore);
        }
        server.close();
        System.exit(0);

    }


    private static void setOpeningBalances(String serverHost) {
        final Client client = new Client(2342323, serverHost, Server.DEFAULT_SERVER_PORT); // Unused seed
        client.connect();

        for (int i = 1; i <= MAX_SEED; i++) {
            final long address = Client.addressFromSeed(i);
            final OpeningBalance openingBalance = new OpeningBalance()
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .init(address, START_AMOUNT);
            client.sendMsg(openingBalance);
            System.out.println("Setting seed " + i + " address " + address + " to " + START_AMOUNT);
        }

        client.close();
    }



}