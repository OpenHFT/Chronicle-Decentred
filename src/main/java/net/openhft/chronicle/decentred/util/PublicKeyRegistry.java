package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.BytesStore;

public interface PublicKeyRegistry {
    void register(long address, BytesStore publicKey);

    Boolean verify(long address, BytesStore sigAndMsg);

    boolean internal();

    PublicKeyRegistry internal(boolean internal);
}
