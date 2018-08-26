package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.api.TransactionProcessor;
import net.openhft.chronicle.decentred.dto.*;

public class VanillaTransactionProcessor implements SystemMessages, TransactionProcessor {
    private MessageRouter<SystemMessages> router;

    @Override
    public void messageRouter(MessageRouter messageRouter) {
        this.router = messageRouter;
    }

    @Override
    public void createAddressRequest(CreateAddressRequest createAddressRequest) {
        router.to(createAddressRequest.address())
                .createAddressEvent(new CreateAddressEvent().createAddressRequest(createAddressRequest));
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
