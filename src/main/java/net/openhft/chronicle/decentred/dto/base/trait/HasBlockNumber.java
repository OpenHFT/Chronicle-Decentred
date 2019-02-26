package net.openhft.chronicle.decentred.dto.base.trait;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;

public interface HasBlockNumber<T extends VanillaSignedMessage<T>> {

    /**
     * Returns the block number for this message.
     *
     * @return the block number for this message
     */
    long blockNumber();

    /**
     * Sets the block number for this message.
     * <p>
     * The block number must be non-negative.
     *
     * @param blockNumber used for setting this message's block number
     *
     * @return this message including the newly updated
     * block number
     */
    T blockNumber(long blockNumber);
}
