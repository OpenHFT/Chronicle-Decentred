package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.decentred.api.SystemMessages;

public interface Gateway extends SystemMessages, Closeable {
    void start();
}
