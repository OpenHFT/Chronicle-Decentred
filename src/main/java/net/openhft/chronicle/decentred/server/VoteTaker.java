package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.internal.server.VanillaVoteTaker;
import net.openhft.chronicle.decentred.server.trait.HasTcpMessageListener;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import org.jetbrains.annotations.NotNull;

public interface VoteTaker extends HasTcpMessageListener {

    /**
     * Receives a TransactionBlockVoteEvent from a Voter.
     *
     * @param transactionBlockVoteEvent vote to consider
     */
    void transactionBlockVoteEvent(@NotNull TransactionBlockVoteEvent transactionBlockVoteEvent);

    /**
     * Returns if this VoteTake has a majority vote.
     *
     * @return if this VoteTake has a majority vote
     */
    boolean hasMajority();

    /**
     * Instructs this VoteTaker to send a EndOfRoundBlockEvent for the  provided
     * {@code blockNumber}
     *
     * @param blockNumber to use
     *
     * @return if at least one block event was replayed, false otherwise
     */
    boolean sendEndOfRoundBlock(long blockNumber);

    @NotNull
    static VoteTaker create(long address,
                            long chainAddress,
                            @NotNull long[] clusterAddresses,
                            @NotNull BlockReplayer replayer,
                            @NotNull BytesStore secretKey,
                            @NotNull DtoRegistry dtoRegistry) {
        return new VanillaVoteTaker(address, chainAddress, clusterAddresses, replayer, secretKey, dtoRegistry);
    }
}
