package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.dto.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.util.LongLongMap;

public class VanillaGossiper implements Gossiper {
    private static final Long NO_BLOCK = -1L;
    private final long address;
    private final LongLongMap lastBlockMap = LongLongMap.withExpectedSize(16);
    private final LongLongMap lastVoteMap;
    private final long[] clusterAddresses;
    private final TransactionBlockGossipEvent gossip;
    private MessageRouter<Voter> lookup;

    public VanillaGossiper(long address, long chainAddress, long[] clusterAddresses) {
        this.address = address;
        this.clusterAddresses = clusterAddresses;
        lastVoteMap = LongLongMap.withExpectedSize(16);
        gossip = new TransactionBlockGossipEvent().chainAddress(chainAddress);
    }

    @Override
    public synchronized void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent) {
        //System.out.println(address + " " + transactionBlockEvent);
        long sourceAddress = transactionBlockEvent.address();
        if (sourceAddress == 0) {
            System.err.println("Missing sourceAddress " + transactionBlockEvent);
            return;
        }
        long lastBlockNumber = lastBlockMap.getOrDefault(sourceAddress, NO_BLOCK);
        long blockNumber = transactionBlockEvent.blockNumber();
        if (lastBlockNumber < blockNumber)
            lastBlockMap.justPut(sourceAddress, blockNumber);
    }

    @Override
    public void sendGossip(long blockNumber) {
        gossip.reset();
        gossip.address(address);
        gossip.blockNumber(blockNumber);
        synchronized (this) {
            lastVoteMap.putAll(lastBlockMap);
        }
        for (long clusterAddress : clusterAddresses) {
            lookup.to(clusterAddress).transactionBlockGossipEvent(gossip);
        }
    }
}
