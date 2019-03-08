package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.internal.server.LastGossipVoter;
import net.openhft.chronicle.decentred.server.trait.HasTcpMessageListener;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import org.jetbrains.annotations.NotNull;

public interface Voter extends HasTcpMessageListener {

    void transactionBlockGossipEvent(@NotNull TransactionBlockGossipEvent transactionBlockGossipEvent);

    void sendVote(long blockNumber);

    static Voter createLastGossipVoter(long address,
                                       @NotNull long[] clusterAddresses,
                                       @NotNull VanillaVoteTaker voteTaker,
                                       @NotNull BytesStore secretKey,
                                       @NotNull DtoRegistry dtoRegistry) {
        return new LastGossipVoter(address, clusterAddresses, voteTaker, secretKey, dtoRegistry);
    }

}
