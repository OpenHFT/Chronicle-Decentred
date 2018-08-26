package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.annotation.UsedViaReflection;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.api.Verifier;
import net.openhft.chronicle.decentred.dto.*;
import net.openhft.chronicle.decentred.remote.net.TCPConnection;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.PublicKeyRegistry;
import net.openhft.chronicle.decentred.util.VanillaPublicKeyRegistry;

/**
 * This accepts message from the RPCServer and passes them to the appropriate downstream component
 */
public class VanillaGateway implements Gateway {
    private static final long MAIN_CHAIN = DecentredUtil.parseAddress("main");
    //    private final long address;
    private final long chainAddress;
    private final BlockEngine main;
    private final BlockEngine local;
    private final VanillaVerifyIP verifyIP;
    private MessageRouter router;
    private PublicKeyRegistry publicKeyRegistry = new VanillaPublicKeyRegistry();

    public VanillaGateway(long address, long chainAddress, BlockEngine main, BlockEngine local) {
//        this.address = address;
        this.chainAddress = chainAddress;
        this.main = main;
        this.local = local;
        verifyIP = new VanillaVerifyIP(addr -> (Verifier) router.to(addr));
    }

    public static <T> VanillaGateway newGateway(DtoRegistry<T> dtoRegistry,
                                                long address,
                                                String regionStr,
                                                long[] clusterAddresses,
                                                int mainPeriodMS,
                                                int localPeriodMS,
                                                T mainTransactionProcessor,
                                                T localTransactionPrcoessor) {
        long region = DecentredUtil.parseAddress(regionStr);
        return new VanillaGateway(address,
                region,
                VanillaBlockEngine.newMain(dtoRegistry, address, mainPeriodMS, clusterAddresses, mainTransactionProcessor),
                VanillaBlockEngine.newLocal(dtoRegistry, address, region, localPeriodMS, clusterAddresses, localTransactionPrcoessor)
        );
    }

    public void start(MessageToListener messageToListener) {
        main.start(messageToListener);
        local.start(messageToListener);
    }

    public void tcpMessageListener(MessageToListener messageToListener) {
        main.tcpMessageListener(messageToListener);
        local.tcpMessageListener(messageToListener);
    }

    @UsedViaReflection
    public void processOneBlock() {
        main.processOneBlock();
        local.processOneBlock();
    }

    public VanillaGateway router(MessageRouter router) {
        this.router = router;
        return this;
    }

    @Override
    public void createAddressRequest(CreateAddressRequest createAddressRequest) {
        main.onMessage(createAddressRequest);
    }

    @Override
    public void verificationEvent(VerificationEvent verificationEvent) {
        verifyIP.verificationEvent(verificationEvent);
    }

    @Override
    public void invalidationEvent(InvalidationEvent invalidationEvent) {
        verifyIP.invalidationEvent(invalidationEvent);
    }

    @Override
    public void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent) {
        long chainAddress = transactionBlockEvent.chainAddress();
        if (isMainChain(chainAddress))
            main.transactionBlockEvent(transactionBlockEvent);
        else if (this.chainAddress == chainAddress)
            local.transactionBlockEvent(transactionBlockEvent);
        else
            System.err.println("Unknown chainAddress " + DecentredUtil.toAddressString(chainAddress));
    }

    private boolean isMainChain(long chainAddress) {
        return chainAddress == MAIN_CHAIN;
    }

    @Override
    public void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent) {
        long chainAddress = transactionBlockGossipEvent.chainAddress();
        if (isMainChain(chainAddress))
            main.transactionBlockGossipEvent(transactionBlockGossipEvent);
        else if (this.chainAddress == chainAddress)
            local.transactionBlockGossipEvent(transactionBlockGossipEvent);
        else
            System.err.println("Unknown chainAddress " + DecentredUtil.toAddressString(chainAddress));
    }

    @Override
    public void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent) {
        long chainAddress = transactionBlockVoteEvent.chainAddress();
        if (isMainChain(chainAddress))
            main.transactionBlockVoteEvent(transactionBlockVoteEvent);
        else if (this.chainAddress == chainAddress)
            local.transactionBlockVoteEvent(transactionBlockVoteEvent);
        else
            System.err.println("Unknown chainAddress " + DecentredUtil.toAddressString(chainAddress));
    }

    @Override
    public void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent) {
        long chainAddress = endOfRoundBlockEvent.chainAddress();
        if (isMainChain(chainAddress))
            main.endOfRoundBlockEvent(endOfRoundBlockEvent);
        else if (this.chainAddress == chainAddress)
            local.endOfRoundBlockEvent(endOfRoundBlockEvent);
        else
            System.err.println("Unknown chainAddress " + this.chainAddress);
    }

    public void createAccountEvent(CreateAddressEvent createAddressEvent) {
        // received as a weekly event
        checkTrusted(createAddressEvent);
        publicKeyRegistry.register(createAddressEvent.address(),
                createAddressEvent.publicKey());
    }

    private void checkTrusted(SignedMessage message) {

    }

    @Override
    public void close() {
//        main.close();
//        local.close();
    }

    @Override
    public void onConnection(TCPConnection connection) {
        Gateway.super.onConnection(connection);
        verifyIP.onConnection(connection);
    }

}
