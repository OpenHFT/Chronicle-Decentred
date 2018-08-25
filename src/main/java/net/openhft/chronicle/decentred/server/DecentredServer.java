package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.util.PublicKeyRegistry;

public interface DecentredServer<T> extends MessageRouter<T>, MessageListener, PublicKeyRegistry {
}
