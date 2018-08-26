package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.decentred.dto.SignedMessage;

public interface MessageListener {
    void onMessage(SignedMessage message);
}
