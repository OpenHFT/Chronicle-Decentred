package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageToListener;

public interface RunningMessageToListener extends MessageToListener {
    Runnable[] runnables();
}
