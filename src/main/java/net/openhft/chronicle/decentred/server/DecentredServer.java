package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.util.AddressConverter;
import net.openhft.chronicle.decentred.util.PublicKeyRegistry;
import net.openhft.chronicle.wire.LongConversion;

public interface DecentredServer<T> extends MessageRouter<T>, MessageToListener, PublicKeyRegistry {
    void subscribe(@LongConversion(AddressConverter.class) long address);
}
