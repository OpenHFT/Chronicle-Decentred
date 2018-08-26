package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.util.PublicKeyRegistry;

public interface DecentredServer<T> extends MessageRouter<T>, MessageToListener, PublicKeyRegistry {
}
