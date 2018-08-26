package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.wire.LongConversion;

public interface PublicKeyRegistry {
    void register(@LongConversion(AddressConverter.class) long address, BytesStore publicKey);

    Boolean verify(@LongConversion(AddressConverter.class) long address, BytesStore sigAndMsg);

    boolean internal();

    PublicKeyRegistry internal(boolean internal);
}
