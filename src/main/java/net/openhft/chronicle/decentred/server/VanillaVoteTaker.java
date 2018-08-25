package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageListener;
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
    private MessageListener lookup;
    private LongLongMap addressToBlockNumberMap = LongLongMap.withExpectedSize(16);
    private EndOfRoundBlockEvent endOfRoundBlockEvent = new EndOfRoundBlockEvent();

    public VanillaVoteTaker(MessageListener lookup, long address, long chainAddress, long[] clusterAddresses) {
        assert LongStream.of(clusterAddresses).distinct().count() == clusterAddresses.length;

        this.address = address;
        this.clusterAddresses = clusterAddresses;
        this.chainAddress = chainAddress;
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
    public void sendEndOfRoundBlock(long blockNumber) {
        // TODO only do this when a majority of nodes vote the same.
        // TODO see previous method on determining the majority.
        endOfRoundBlockEvent.reset();
        endOfRoundBlockEvent.address(address)
                .chainAddress(chainAddress);
        synchronized (this) {
            assert !addressToBlockNumberMap.containsKey(0L);
            endOfRoundBlockEvent.blockRecords().putAll(addressToBlockNumberMap);
        }
        endOfRoundBlockEvent.blockNumber(blockNumber);
        for (long clusterAddress : clusterAddresses) {
            lookup.onMessage(clusterAddress, endOfRoundBlockEvent);
        }
    }
}
