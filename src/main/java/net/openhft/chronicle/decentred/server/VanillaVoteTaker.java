package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.dto.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.util.LongLongMap;

import java.util.stream.LongStream;

// TODO only take a majority view rather than last one wins.
// TODO might need a stage before this where the servers announce a proposed EndOfRoundBlock.

public class VanillaVoteTaker implements VoteTaker {
    private final long address;
    private final long[] clusterAddresses;
    private final long chainAddress;
    private final BlockReplayer replayer;
    private LongLongMap addressToBlockNumberMap = LongLongMap.withExpectedSize(16);
    private EndOfRoundBlockEvent endOfRoundBlockEvent = new EndOfRoundBlockEvent();
    private MessageToListener tcpMessageListener;

    public VanillaVoteTaker(long address, long chainAddress, long[] clusterAddresses, BlockReplayer replayer) {
        assert LongStream.of(clusterAddresses).distinct().count() == clusterAddresses.length;

        this.address = address;
        this.clusterAddresses = clusterAddresses;
        this.chainAddress = chainAddress;
        this.replayer = replayer;
    }

    @Override
    public void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent) {
        //System.out.println(address + " " + transactionBlockVoteEvent);
        LongLongMap addressToBlockNumberMap = transactionBlockVoteEvent.gossipEvent().addressToBlockNumberMap();
        assert !addressToBlockNumberMap.containsKey(0L);
        this.addressToBlockNumberMap.putAll(addressToBlockNumberMap);
    }

    public boolean hasMajority() {
        return true;
    }

    @Override
    public boolean sendEndOfRoundBlock(long blockNumber) {
        // TODO only do this when a majority of nodes vote the same.
        // TODO see previous method on determining the majority.
        endOfRoundBlockEvent.reset();
        endOfRoundBlockEvent.address(address)
                .chainAddress(chainAddress);
        synchronized (this) {
            if (addressToBlockNumberMap.size() == 0) {
//                Jvm.warn().on(getClass(), "No blocks to complete");
                return false;
            }
            endOfRoundBlockEvent.addressToBlockNumberMap().putAll(addressToBlockNumberMap);
        }
        endOfRoundBlockEvent.blockNumber(blockNumber);
        for (long clusterAddress : clusterAddresses) {
            if (clusterAddress == address)
                replayer.endOfRoundBlockEvent(endOfRoundBlockEvent);
            else
                tcpMessageListener.onMessageTo(clusterAddress, endOfRoundBlockEvent);
        }
        return true;
    }

    public VanillaVoteTaker tcpMessageListener(MessageToListener tcpMessageListener) {
        this.tcpMessageListener = tcpMessageListener;
        return this;
    }
}
