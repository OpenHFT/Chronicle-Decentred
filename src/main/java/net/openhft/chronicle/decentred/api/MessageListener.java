package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.decentred.dto.base.SignedMessage;

public interface MessageListener {

    /**
     * Called by the framework on a signed message
     *
     * @param message connected
     */
    void onMessage(SignedMessage message);
}
