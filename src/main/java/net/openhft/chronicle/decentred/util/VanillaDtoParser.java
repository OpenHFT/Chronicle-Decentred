package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;

public class VanillaDtoParser<T> implements DtoParser<T> {
    private final XCLIntObjMap<DtoParselet> parseletMap;

    public VanillaDtoParser(XCLIntObjMap<DtoParselet> parseletMap) {
        this.parseletMap = parseletMap;
    }

    @Override
    public void parseOne(Bytes bytes, T listener) {
        int protocolMessageType = bytes.readInt(bytes.readPosition() + VanillaSignedMessage.MESSAGE_TYPE);
        DtoParselet parselet = parseletMap.get(protocolMessageType);
        parselet.parse(bytes, listener);
    }
}
