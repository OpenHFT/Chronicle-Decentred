package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.LongLongMap;

import java.util.stream.LongStream;

public class VanillaGossiper implements Gossiper {
    private static final Long NO_BLOCK = -1L;
    private final long address;
    private final LongLongMap lastBlockMap = LongLongMap.withExpectedSize(16);
    private final long[] clusterAddresses;
    private final Voter voter;
    private final long chainAddress;
    private MessageToListener tcpMessageToListener;
    private final BytesStore secretKey;
    private final DtoRegistry<SystemMessages> dtoRegistry;

    public VanillaGossiper(long address, long chainAddress, long[] clusterAddresses, Voter voter, BytesStore secretKey, DtoRegistry dtoRegistry) {
        this.address = address;
        this.clusterAddresses = clusterAddresses;
        this.voter = voter;
        this.chainAddress = chainAddress;
        this.secretKey = secretKey;
        this.dtoRegistry = dtoRegistry;  // All registries can handle system messages
        assert LongStream.of(clusterAddresses).anyMatch(a -> a == address);
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
        long blockNumber = transactionBlockEvent.timestampUS();
        if (lastBlockNumber < blockNumber)
            lastBlockMap.justPut(sourceAddress, blockNumber);
    }

    @Override
    public void sendGossip(long blockNumber) {
        if (lastBlockMap.size() == 0) {
//            Jvm.warn().on(getClass(), "nothing to gossip about");
            return;
        }

        TransactionBlockGossipEvent gossip = dtoRegistry.create(TransactionBlockGossipEvent.class)
            .chainAddress(chainAddress)
            .address(address);
        synchronized (this) {
            gossip.addressToBlockNumberMap().putAll(lastBlockMap);
        }
        gossip.sign(secretKey);
        for (long clusterAddress : clusterAddresses) {
            if (clusterAddress == address)
                voter.transactionBlockGossipEvent(gossip);
            else
                tcpMessageToListener.onMessageTo(clusterAddress, gossip);
        }
    }

    public VanillaGossiper tcpMessageToListener(MessageToListener tcpMessageToListener) {
        this.tcpMessageToListener = tcpMessageToListener;
        return this;
    }
}
