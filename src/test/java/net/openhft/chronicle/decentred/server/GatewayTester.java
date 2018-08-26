package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.annotation.UsedViaReflection;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.api.SystemMessages;

public interface GatewayTester extends SystemMessages, MessageRouter, MessageToListener {
    @UsedViaReflection
    void processOneBlock();
}
