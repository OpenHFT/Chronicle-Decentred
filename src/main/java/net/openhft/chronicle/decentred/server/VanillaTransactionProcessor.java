package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.*;

public class VanillaTransactionProcessor implements SystemMessages {
    private final SystemMessages messages;

    public VanillaTransactionProcessor(SystemMessages messages) {
        this.messages = messages;
    }

    @Override
    public void createAccountRequest(CreateAddressRequest createAddressRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void verificationEvent(VerificationEvent verificationEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidationEvent(InvalidationEvent record) {
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
    public void createAccountEvent(CreateAddressEvent createAddressEvent) {
        throw new UnsupportedOperationException();
    }
}
