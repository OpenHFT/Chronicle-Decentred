package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.decentred.util.PublicKeyRegistry;
import net.openhft.chronicle.wire.LongConversion;

public interface DecentredServer<U> extends MessageRouter<U>, MessageToListener, PublicKeyRegistry {
    void subscribe(@LongConversion(AddressLongConverter.class) long address);
}
