package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.dto.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.decentred.util.LongLongMap;

import java.util.stream.LongStream;

public class VanillaGossiper implements Gossiper {
    private static final Long NO_BLOCK = -1L;
    private final long address;
    private final LongLongMap lastBlockMap = LongLongMap.withExpectedSize(16);
    private final long[] clusterAddresses;
    private final Voter voter;
    private final TimeProvider timeProvider;
    private final TransactionBlockGossipEvent gossip;
    private MessageToListener tcpMessageToListener;
    private final KeyPair keyPair;
    private final int protocol;
    private final int messageType;

    public VanillaGossiper(KeyPair keyPair, DtoRegistry dtoRegistry, long chainAddress, long[] clusterAddresses, Voter voter, TimeProvider timeProvider) {
        this.keyPair = keyPair;
        this.address = keyPair.address();
        protocol = dtoRegistry.protocolFor(TransactionBlockGossipEvent.class);
        messageType = dtoRegistry.messageTypeFor(TransactionBlockGossipEvent.class);

        this.clusterAddresses = clusterAddresses;
        this.voter = voter;
        this.timeProvider = timeProvider;
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
//            Jvm.warn().on(getClass(), "nothing to gossip about");
            return;
        }

        gossip.reset();
        gossip.protocol(protocol)
                .messageType(messageType)
                .address(address)
                .blockNumber(blockNumber);
        synchronized (this) {
            gossip.addressToBlockNumberMap().putAll(lastBlockMap);
        }
        gossip.sign(keyPair.secretKey, timeProvider);
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
