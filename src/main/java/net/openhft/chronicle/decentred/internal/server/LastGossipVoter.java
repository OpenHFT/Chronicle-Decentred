package net.openhft.chronicle.decentred.internal.server;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.server.VanillaVoteTaker;
import net.openhft.chronicle.decentred.server.Voter;
import net.openhft.chronicle.decentred.util.DtoRegistry;

// Currently votes on the last piece of gossip
public class LastGossipVoter implements Voter {
    private final long address;
    private final long[] clusterAddresses;
    private final VanillaVoteTaker voteTaker;
    private MessageToListener tcpMessageListener;
    private TransactionBlockGossipEvent receivedGossip = new TransactionBlockGossipEvent();
    private final DtoRegistry<SystemMessages> dtoRegistry;
    private final BytesStore secretKey;

    public LastGossipVoter(long address, long[] clusterAddresses, VanillaVoteTaker voteTaker, BytesStore secretKey, DtoRegistry dtoRegistry) {
        this.address = address;
        this.clusterAddresses = clusterAddresses;
        this.voteTaker = voteTaker;
        this.secretKey = secretKey;
        this.dtoRegistry = dtoRegistry;  // All registries can handle system messages
    }

    @Override
    public synchronized void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent) {
        receivedGossip = transactionBlockGossipEvent;
    }

    @Override
    public synchronized void sendVote(long blockNumber) {
        if (receivedGossip.addressToBlockNumberMap().size() == 0) {
//            Jvm.warn().on(getClass(), "Nothing to vote on");
            return;
        }
        TransactionBlockVoteEvent vote = dtoRegistry.create(TransactionBlockVoteEvent.class)
            .gossipEvent(receivedGossip);
        vote.sign(secretKey);
        for (long clusterAddress : clusterAddresses) {
            if (address == clusterAddress)
                voteTaker.transactionBlockVoteEvent(vote);
            else
                tcpMessageListener.onMessageTo(clusterAddress, vote);
        }
    }

    public LastGossipVoter tcpMessageListener(MessageToListener tcpMessageListener) {
        this.tcpMessageListener = tcpMessageListener;
        return this;
    }
}
