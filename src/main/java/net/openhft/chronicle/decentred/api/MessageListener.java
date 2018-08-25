package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.decentred.dto.SignedMessage;

public interface MessageListener {
    default void onMessage(SignedMessage message) {
        onMessageTo(MessageRouter.DEFAULT_CONNECTION, message);
    }

    default void onMessageTo(long address, SignedMessage message) {
        if (address == MessageRouter.DEFAULT_CONNECTION)
            onMessage(message);
        throw new UnsupportedOperationException();
    }
}
