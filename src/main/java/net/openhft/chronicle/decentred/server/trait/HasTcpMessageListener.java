package net.openhft.chronicle.decentred.server.trait;

import net.openhft.chronicle.decentred.api.MessageToListener;
import org.jetbrains.annotations.NotNull;

public interface HasTcpMessageListener {

    /**
     * Sets the TCP Message Listener for this object.
     *
     * @param messageToListener to set
     */
    void tcpMessageListener(@NotNull MessageToListener messageToListener);
}
