package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.blockevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.LongLongMap;
import org.jetbrains.annotations.NotNull;

import java.util.stream.LongStream;

// TODO only take a majority view rather than last sample wins.
// TODO might need a stage before this where the servers announce a proposed EndOfRoundBlock.

public class VanillaVoteTaker implements VoteTaker {
    private final long address;
    private final long[] clusterAddresses;
    private final long chainAddress;
    private final BlockReplayer replayer;
    private LongLongMap addressToBlockNumberMap = LongLongMap.withExpectedSize(16);
    private MessageToListener tcpMessageListener;
    private final BytesStore secretKey;
    private final DtoRegistry<SystemMessages> dtoRegistry;

    public VanillaVoteTaker(long address, long chainAddress, long[] clusterAddresses, BlockReplayer replayer, BytesStore secretKey, DtoRegistry dtoRegistry) {
        this.secretKey = secretKey;
        assert LongStream.of(clusterAddresses).distinct().count() == clusterAddresses.length;

        this.address = address;
        this.clusterAddresses = clusterAddresses;
        this.chainAddress = chainAddress;
        this.replayer = replayer;
        this.dtoRegistry = dtoRegistry;
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

        EndOfRoundBlockEvent endOfRoundBlockEvent;
        synchronized (this) {
            if (addressToBlockNumberMap.size() == 0) {
//                Jvm.warn().on(getClass(), "No blocks to complete");
                return false;
            }
            endOfRoundBlockEvent = dtoRegistry.create(EndOfRoundBlockEvent.class)
                .address(address)
                .chainAddress(chainAddress);
            endOfRoundBlockEvent.addressToBlockNumberMap().putAll(addressToBlockNumberMap);
        }
        endOfRoundBlockEvent.sign(secretKey);

        for (long clusterAddress : clusterAddresses) {
            if (clusterAddress == address)
                replayer.endOfRoundBlockEvent(endOfRoundBlockEvent);
            else
                tcpMessageListener.onMessageTo(clusterAddress, endOfRoundBlockEvent);
        }
        return true;
    }

    public void tcpMessageListener(@NotNull MessageToListener tcpMessageListener) {
        this.tcpMessageListener = tcpMessageListener;
    }
}
