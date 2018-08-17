package net.openhft.chronicle.decentred.remote.net;

import net.openhft.chronicle.core.io.Closeable;

public interface TCPServer extends Closeable {
    TCPServerConnectionListener connectionListener();
}
