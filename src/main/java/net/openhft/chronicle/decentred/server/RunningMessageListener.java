package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageListener;

public interface RunningMessageListener extends MessageListener {
    Runnable[] runnables();
}
