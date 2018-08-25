package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.dto.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockVoteEvent;

public class VanillaVoter implements Voter {
    private final long[] clusterAddresses;
    private MessageListener router;
    private TransactionBlockGossipEvent gossip = new TransactionBlockGossipEvent();
    private TransactionBlockVoteEvent vote = new TransactionBlockVoteEvent();

    public VanillaVoter(MessageListener router, long[] clusterAddresses) {
        this.router = router;
        this.clusterAddresses = clusterAddresses;
    }

    @Override
    public synchronized void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent) {
        transactionBlockGossipEvent.copyTo(this.gossip);
    }

    @Override
    public void sendVote(long blockNumber) {
        vote.reset();
        synchronized (this) {
            gossip.copyTo(vote.gossipEvent());
        }
        for (long clusterAddress : clusterAddresses) {
            router.onMessageTo(clusterAddress, vote);
        }
    }
}
