package net.openhft.chronicle.decentred.dto.base.trait;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;

public interface HasChainAddress<T extends VanillaSignedMessage<T>> {

    /**
     * Returns the chain address for this message.
     *
     * @return the chain address for this message
     */
    long chainAddress();

    /**
     * Sets the chain address for this message.
     * <p>
     * A chain address uniquely identifies a chain.
     *
     * @param chainAddress used for setting this message's chain address
     *
     * @return this message including the newly updated
     * chain address
     */
    T chainAddress(long chainAddress);
}
