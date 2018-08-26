package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.dto.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockVoteEvent;

public class VanillaVoter implements Voter {
    private final long[] clusterAddresses;
    private MessageToListener tcpMessageListener;
    private TransactionBlockGossipEvent gossip = new TransactionBlockGossipEvent();
    private TransactionBlockVoteEvent vote = new TransactionBlockVoteEvent();

    public VanillaVoter(long[] clusterAddresses) {
        this.clusterAddresses = clusterAddresses;
    }

    @Override
    public synchronized void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent) {
        System.out.println("Received " + transactionBlockGossipEvent);
        transactionBlockGossipEvent.copyTo(this.gossip);
    }

    @Override
    public void sendVote(long blockNumber) {
        if (gossip.addressToBlockNumberMap().size() == 0) {
            Jvm.warn().on(getClass(), "Nothing to vote on");
            return;
        }
        vote.reset();
        synchronized (this) {
            gossip.copyTo(vote.gossipEvent());
        }
        for (long clusterAddress : clusterAddresses) {
            tcpMessageListener.onMessageTo(clusterAddress, vote);
        }
    }

    public VanillaVoter tcpMessageListener(MessageToListener tcpMessageListener) {
        this.tcpMessageListener = tcpMessageListener;
        return this;
    }
}
