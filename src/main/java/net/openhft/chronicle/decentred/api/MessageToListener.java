package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.decentred.dto.SignedMessage;
import net.openhft.chronicle.decentred.util.AddressConverter;
import net.openhft.chronicle.wire.LongConversion;

public interface MessageToListener {
    void onMessageTo(@LongConversion(AddressConverter.class) long address, SignedMessage message);
}
