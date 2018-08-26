package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.api.SystemMessages;

public interface BlockEngine extends SystemMessages, MessageListener {
    void start(MessageToListener messageToListener);

    // Used for testing.
    void tcpMessageListener(MessageToListener messageToListener);

    // Used for testing.
    void processOneBlock();
}
