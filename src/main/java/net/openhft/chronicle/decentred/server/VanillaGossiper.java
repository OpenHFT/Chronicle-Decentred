package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.dto.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.util.LongLongMap;

import java.util.stream.LongStream;

public class VanillaGossiper implements Gossiper {
    private static final Long NO_BLOCK = -1L;
    private final long address;
    private final LongLongMap lastBlockMap = LongLongMap.withExpectedSize(16);
    private final long[] clusterAddresses;
    private final TransactionBlockGossipEvent gossip;
    private MessageToListener tcpMessageToListener;

    public VanillaGossiper(long address, long chainAddress, long[] clusterAddresses) {
        this.address = address;
        this.clusterAddresses = clusterAddresses;
        assert LongStream.of(clusterAddresses).anyMatch(a -> a == address);
        gossip = new TransactionBlockGossipEvent()
                .chainAddress(chainAddress);
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
        if (lastBlockMap.size() == 0) {
            Jvm.warn().on(getClass(), "nothing to gossip about");
            return;
        }

        gossip.reset();
        gossip.address(address);
        gossip.blockNumber(blockNumber);
        synchronized (this) {
            gossip.addressToBlockNumberMap().putAll(lastBlockMap);
        }
        for (long clusterAddress : clusterAddresses) {
            tcpMessageToListener.onMessageTo(clusterAddress, gossip);
        }
    }

    public VanillaGossiper tcpMessageToListener(MessageToListener tcpMessageToListener) {
        this.tcpMessageToListener = tcpMessageToListener;
        return this;
    }
}
