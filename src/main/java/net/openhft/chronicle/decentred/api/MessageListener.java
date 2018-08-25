package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.decentred.dto.SignedMessage;

public interface MessageListener {
    default void onMessage(SignedMessage message) {
        onMessage(0, message);
    }

    default void onMessage(long address, SignedMessage message) {
        if (address == 0)
            onMessage(message);
        throw new UnsupportedOperationException();
    }
}
