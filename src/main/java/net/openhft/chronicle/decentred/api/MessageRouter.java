package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.decentred.util.AddressConverter;
import net.openhft.chronicle.wire.LongConversion;

public interface MessageRouter<T> {
    long DEFAULT_CONNECTION = 0L;

    T to(@LongConversion(AddressConverter.class) long address);
}
