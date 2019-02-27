package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.BlockchainPhase;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.api.TransactionProcessor;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.address.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.chainevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.dto.chainlifecycle.AssignDelegatesRequest;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateChainRequest;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateTokenRequest;

public class VanillaTransactionProcessor implements SystemMessages, TransactionProcessor {
    private MessageRouter<SystemMessages> router;
    private BlockchainPhase blockchainPhase;

    @Override
    public void messageRouter(MessageRouter messageRouter) {
        this.router = messageRouter;
    }

    @Override
    public void blockchainPhase(BlockchainPhase blockchainPhase) {
        this.blockchainPhase = blockchainPhase;
    }

    @Override
    public void createChainRequest(CreateChainRequest createChainRequest) {
        // TODO
    }

    @Override
    public void createTokenRequest(CreateTokenRequest createTokenRequest) {
//        System.out.println(createTokenRequest);
        // TODO
    }

    @Override
    public void assignDelegatesRequest(AssignDelegatesRequest assignDelegatesRequest) {
        // TODO
    }

    @Override
    public void createAddressRequest(CreateAddressRequest createAddressRequest) {
        router.to(createAddressRequest.address())
                .createAddressEvent(new CreateAddressEvent()
                        .createAddressRequest(createAddressRequest));
    }

    @Override
    public void verificationEvent(VerificationEvent verificationEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidationEvent(InvalidationEvent invalidationEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createAddressEvent(CreateAddressEvent createAddressEvent) {
        throw new UnsupportedOperationException();
    }
}
