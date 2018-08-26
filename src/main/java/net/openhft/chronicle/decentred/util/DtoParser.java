package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.Bytes;

public interface DtoParser<T> {
    void parseOne(Bytes bytes, T listener);

    Class<T> superInterface();

    void onMessage(T component, Object message);
}
