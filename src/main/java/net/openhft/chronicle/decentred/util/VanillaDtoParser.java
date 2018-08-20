package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;

public class VanillaDtoParser<T> implements DtoParser<T> {
    private final Class<T> clazz;

    private final IntObjMap<DtoParselet> parseletMap;

    public VanillaDtoParser(Class<T> clazz, IntObjMap<DtoParselet> parseletMap) {
        this.clazz = clazz;
        this.parseletMap = parseletMap;
    }

    @Override
    public void parseOne(Bytes bytes, T listener) {
        int protocolMessageType = bytes.readInt(bytes.readPosition() + VanillaSignedMessage.MESSAGE_TYPE);
        DtoParselet parselet = parseletMap.get(protocolMessageType);
        if (parselet == null)
            Jvm.warn().on(getClass(), "Unable to find a parselet for protocol " + (protocolMessageType >>> 16) + " messageType " + (protocolMessageType & 0xFFFF));
        else
            parselet.parse(bytes, listener);
    }

    @Override
    public Class<T> superInterface() {
        return clazz;
    }
}
