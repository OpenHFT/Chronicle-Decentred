package town.lost.examples.appreciation.benchmark;

import net.openhft.chronicle.decentred.api.BlockchainPhase;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.TransactionProcessor;
import net.openhft.chronicle.decentred.dto.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.server.BlockEngine;
import net.openhft.chronicle.decentred.server.GatewayConfiguration;
import net.openhft.chronicle.decentred.server.VanillaBlockEngine;
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

    private final RPCServer<AppreciationMessages, AppreciationRequests> rpcServer;

    private Server(int seed, int port, BalanceStore balanceStore) throws IOException {
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
                config.address(), mainEngine, localEngine, messageRouter, blockChain, balanceStore);
        };
        rpcServer = getRpcBuilder().createServer(port, mainProcessor, localProcessor, gatewayConstructor);
        ((TransactionProcessor) mainProcessor).messageRouter(rpcServer);
        ((TransactionProcessor) localProcessor).messageRouter(rpcServer);
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

        while (true) {
            Thread.sleep(5000);
            System.out.println("balanceStore = " + balanceStore);
        }

    }

}