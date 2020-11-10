package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.wire.LongConversion;

public interface MessageRouter<T> {
    long DEFAULT_CONNECTION = 0L;

    default T toDefault() {
        return to(DEFAULT_CONNECTION);
    }

    T to(@LongConversion(AddressLongConverter.class) long address);
}
