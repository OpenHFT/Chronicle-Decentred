package net.openhft.chronicle.decentred.server;


import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;

public interface Gossiper {

    /**
     * Receives a TransactionBlockEvent from a 999 node.
     *
     * @param transactionBlockEvent to receive
     */
    void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent);

    /**
     * Instructs this Gossiper to send a TransactionBlockGossipEvent for the provided
     * {@code blockNumber} to a Voter
     *
     * @param blockNumber to use
     */
    void sendGossip(long blockNumber);
}
