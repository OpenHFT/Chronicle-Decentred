package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.internal.server.LastGossipVoter;
import net.openhft.chronicle.decentred.util.DtoRegistry;

public interface Voter {

    void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent);

    void sendVote(long blockNumber);

    static Voter lastGossipVoter(long address, long[] clusterAddresses, VanillaVoteTaker voteTaker, BytesStore secretKey, DtoRegistry dtoRegistry) {
        return new LastGossipVoter(address, clusterAddresses, voteTaker, secretKey, dtoRegistry);
    }

}
