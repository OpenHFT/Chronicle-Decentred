package net.openhft.chronicle.decentred.dto.base.trait;

import net.openhft.chronicle.decentred.dto.base.TransientFieldHandler;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;

public interface HasTransientFieldHandler<T extends VanillaSignedMessage<T>> {

    TransientFieldHandler<T> transientFieldHandler();

}
