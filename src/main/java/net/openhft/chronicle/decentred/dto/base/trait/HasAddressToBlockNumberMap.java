package net.openhft.chronicle.decentred.dto.base.trait;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.LongLongMap;

public interface HasAddressToBlockNumberMap<T extends VanillaSignedMessage<T>> {

    /**
     * Returns the address to block number map for this message.
     * <p>
     * The returned map is mutable.
     *
     * @return the address to block number map for this message
     */
    LongLongMap addressToBlockNumberMap();

}
