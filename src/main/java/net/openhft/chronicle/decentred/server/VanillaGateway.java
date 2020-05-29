package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.annotation.UsedViaReflection;
import net.openhft.chronicle.core.io.AbstractCloseable;
import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.decentred.api.Verifier;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.address.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.dto.blockevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.dto.chainlifecycle.AssignDelegatesRequest;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateChainRequest;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateTokenRequest;
import net.openhft.chronicle.decentred.remote.net.TCPConnection;
import net.openhft.chronicle.decentred.util.*;

/**
 * This accepts message from the RPCServer and passes them to the appropriate downstream component
 */
public class VanillaGateway extends AbstractCloseable implements Gateway {
    private static final long MAIN_CHAIN = DecentredUtil.parseAddress("main");
    //    private final long address;
    private final long chainAddress;
    private final BlockEngine main;
    private final BlockEngine local;
    private final VanillaVerifyIP verifyIP;
    private DecentredServer decentredServer;
    private PublicKeyRegistry publicKeyRegistry = new VanillaPublicKeyRegistry();

    public VanillaGateway(long address, long chainAddress, BlockEngine main, BlockEngine local) {
//        this.address = address;
        this.chainAddress = chainAddress;
        this.main = main;
        this.local = local;
        verifyIP = new VanillaVerifyIP(addr -> (Verifier) decentredServer.to(addr));
    }

    public static <T> VanillaGateway newGateway(DtoRegistry<T> dtoRegistry,
                                                KeyPair keyPair,
                                                String regionStr,
                                                long[] clusterAddresses,
                                                int mainPeriodMS,
                                                int localPeriodMS,
                                                T mainTransactionProcessor,
                                                T localTransactionProcessor,
                                                TimeProvider timeProvider) {
        long region = DecentredUtil.parseAddress(regionStr);
        return new VanillaGateway(keyPair.address(),
                region,
                BlockEngine.newMain(dtoRegistry, keyPair, mainPeriodMS, clusterAddresses, mainTransactionProcessor, timeProvider),
                BlockEngine.newLocal(dtoRegistry, keyPair, region, localPeriodMS, clusterAddresses, localTransactionProcessor, timeProvider)
        );
    }

    @Override
    public void start(DecentredServer decentredServer) {
        this.decentredServer = decentredServer;
        main.start(decentredServer);
        local.start(decentredServer);
    }

    public void tcpMessageListener(DecentredServer decentredServer) {
        this.decentredServer = decentredServer;
        main.tcpMessageListener(decentredServer);
        local.tcpMessageListener(decentredServer);
    }

    @UsedViaReflection
    public void processOneBlock() {
        main.processOneBlock();
        local.processOneBlock();
    }

    @Override
    public void createAddressRequest(CreateAddressRequest createAddressRequest) {
        BytesStore publicKey = createAddressRequest.publicKey();
        long address = DecentredUtil.toAddress(publicKey);
        decentredServer.register(address, publicKey);
        decentredServer.subscribe(address);
        main.onMessage(createAddressRequest);
    }

    @Override
    public void createChainRequest(CreateChainRequest createChainRequest) {
        main.createChainRequest(createChainRequest);
    }

    @Override
    public void assignDelegatesRequest(AssignDelegatesRequest assignDelegatesRequest) {
        main.assignDelegatesRequest(assignDelegatesRequest);
    }

    @Override
    public void createTokenRequest(CreateTokenRequest createTokenRequest) {
        main.createTokenRequest(createTokenRequest);
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
            logUnknownChainAddress(transactionBlockEvent, chainAddress);
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
            logUnknownChainAddress(transactionBlockGossipEvent, chainAddress);
    }

    @Override
    public void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent) {
        long chainAddress = transactionBlockVoteEvent.chainAddress();
        if (isMainChain(chainAddress))
            main.transactionBlockVoteEvent(transactionBlockVoteEvent);
        else if (this.chainAddress == chainAddress)
            local.transactionBlockVoteEvent(transactionBlockVoteEvent);
        else
            logUnknownChainAddress(transactionBlockVoteEvent, chainAddress);
    }

    @Override
    public void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent) {
        long chainAddress = endOfRoundBlockEvent.chainAddress();
        if (isMainChain(chainAddress))
            main.endOfRoundBlockEvent(endOfRoundBlockEvent);
        else if (this.chainAddress == chainAddress)
            local.endOfRoundBlockEvent(endOfRoundBlockEvent);
        else
            logUnknownChainAddress(endOfRoundBlockEvent, chainAddress);
    }

    private void logUnknownChainAddress(VanillaSignedMessage message, long chainAddress) {
            System.err.format("%s: Unknown chain address %s (raw %d) (known: %s and %s)%n",
            message.getClass().getSimpleName(),
            DecentredUtil.toAddressString(chainAddress),
            chainAddress,
            DecentredUtil.toAddressString(MAIN_CHAIN),
            DecentredUtil.toAddressString(this.chainAddress)
        );
    }

    public void createAddressEvent(CreateAddressEvent createAddressEvent) {
        // received as a weekly event
        checkTrusted(createAddressEvent);
        publicKeyRegistry.register(createAddressEvent.address(),
                createAddressEvent.publicKey());
    }

    private void checkTrusted(SignedMessage message) {

    }

    @Override
    protected void performClose() {
//        main.close();
//        local.close();
    }

    @Override
    public void onConnection(TCPConnection connection) {
        Gateway.super.onConnection(connection);
        verifyIP.onConnection(connection);
    }

}
