package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.decentred.api.MessageRouter;

public interface MessageWriter<T> extends MessageRouter<T>, Closeable {
    Runnable[] runnables();
}
