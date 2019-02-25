package town.lost.examples.appreciation.decomposed;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.dto.*;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.server.BlockEngine;
import net.openhft.chronicle.decentred.server.Gateway;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import town.lost.examples.appreciation.VanillaAppreciationGateway;
import town.lost.examples.appreciation.api.AppreciationMessages;
import town.lost.examples.appreciation.api.AppreciationRequests;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.api.AppreciationTransactions;
import town.lost.examples.appreciation.benchmark.Node;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OnBalance;
import town.lost.examples.appreciation.dto.OpeningBalance;
import town.lost.examples.appreciation.dto.Topup;
import town.lost.examples.appreciation.util.BalanceStore;
import town.lost.examples.appreciation.util.VanillaBalanceStore;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class BlockEngineStarter {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage " + BlockEngineStarter.class.getSimpleName() + " seed localPort");
        }

        final long seed = Long.parseLong(args[0]);
        System.out.println("Seed is " + seed);

        final int port = Integer.parseInt(args[1]);
        System.out.println("Listening on port " + port);
    }

/*        final String mainBlockEngineAddress = args[2];
        System.out.println("mainBlockEngineAddress is " + mainBlockEngineAddress);

        final String localBlockEngineAddress = args[3];
        System.out.println("localBlockEngineAddress is " + localBlockEngineAddress);*/
/*

        final long region = DecentredUtil.parseAddress(DecomposedUtil.REGION);
        final BlockEngine mainBlockEngine = new RemoteClientBlockEngine(mainBlockEngineAddress);
        final BlockEngine localBlockEngine = new RemoteClientBlockEngine(localBlockEngineAddress);

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

        AppreciationTransactions blockChain = new AppreciationTransactions() {
            @Override
            public void openingBalance(OpeningBalance openingBalance) {
                localBlockEngine.onMessage(openingBalance);
            }

            @Override
            public void give(Give give) {
                localBlockEngine.onMessage(give);
            }

            @Override
            public void topup(Topup topup) {
                localBlockEngine.onMessage(topup);
            }
        };


        final BalanceStore balanceStore = new VanillaBalanceStore();
        final Gateway gateway = new VanillaAppreciationGateway(region, mainBlockEngine, localBlockEngine, messageRouter, blockChain, balanceStore);

        final GatewayNode gatewayNode = new GatewayNode(seed, "gateway", port, gateway);

        System.out.println("Press <enter> to exit");
        while (System.in.available() == 0) {}

        gatewayNode.close();

    }

    public static final class GatewayNode extends Node<AppreciationMessages, AppreciationRequests> {

        private final RPCServer<AppreciationMessages, AppreciationRequests> rpcServer;

        public GatewayNode(long seed, String name, int port, Gateway gateway) throws IOException {
            super(seed, AppreciationMessages.class, AppreciationRequests.class);
            rpcServer = getRpcBuilder().createServer(name, port, gateway);
        }

        @Override
        protected void close() {
            rpcServer.close();
        }
    }



    public static final class RemoteClientBlockEngine implements BlockEngine {

        public final String inetAddredd;

        public RemoteClientBlockEngine(String inetAddredd) {
            this.inetAddredd = requireNonNull(inetAddredd);
            System.out.println("inetAddredd = " + inetAddredd);
        }

        @Override
        public void start(MessageToListener messageToListener) {
            throw unsupportedOperationException();
        }

        @Override
        public void tcpMessageListener(MessageToListener messageToListener) {
            throw unsupportedOperationException();
        }

        @Override
        public void processOneBlock() {
            throw unsupportedOperationException();
        }

        @Override
        public void onMessage(SignedMessage message) {
            System.out.println(RemoteClientBlockEngine.class.getSimpleName() + ".onMessage = " + message);
        }

        @Override
        public void createChainRequest(CreateChainRequest createChainRequest) {
            System.out.println(RemoteClientBlockEngine.class.getSimpleName() + ".createChainRequest = " + createChainRequest);
        }

        @Override
        public void createTokenRequest(CreateTokenRequest createTokenRequest) {
            System.out.println(RemoteClientBlockEngine.class.getSimpleName() + ".createTokenRequest = " + createTokenRequest);
        }

        @Override
        public void createAddressRequest(CreateAddressRequest createAddressRequest) {
            System.out.println(RemoteClientBlockEngine.class.getSimpleName() + ".createAddressRequest = " + createAddressRequest);
        }

        @Override
        public void verificationEvent(VerificationEvent verificationEvent) {
            throw unsupportedOperationException();
        }

        @Override
        public void invalidationEvent(InvalidationEvent invalidationEvent) {
            throw unsupportedOperationException();
        }

        @Override
        public void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent) {
            System.out.println(RemoteClientBlockEngine.class.getSimpleName() + ".transactionBlockEvent = " + transactionBlockEvent);
        }

        @Override
        public void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent) {
            System.out.println(RemoteClientBlockEngine.class.getSimpleName() + ".transactionBlockGossipEvent = " + transactionBlockGossipEvent);
        }

        @Override
        public void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent) {
            System.out.println(RemoteClientBlockEngine.class.getSimpleName() + ".transactionBlockVoteEvent = " + transactionBlockVoteEvent);
        }

        @Override
        public void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent) {
            System.out.println(RemoteClientBlockEngine.class.getSimpleName() + ".endOfRoundBlockEvent = " + endOfRoundBlockEvent);
        }

        private UnsupportedOperationException unsupportedOperationException() {
            return new UnsupportedOperationException("Not supported on a " + getClass().getSimpleName());
        }

    }
*/


}
