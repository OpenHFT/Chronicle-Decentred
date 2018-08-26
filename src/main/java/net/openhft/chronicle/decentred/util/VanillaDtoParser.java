package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class VanillaDtoParser<T> implements DtoParser<T> {
    private final Class<T> clazz;

    private final IntObjMap<DtoParselet> parseletMap;
    private final Map<Class, Method> classConsumerMap;

    public VanillaDtoParser(Class<T> clazz, IntObjMap<DtoParselet> parseletMap, Map<Class, Method> classConsumerMap) {
        this.clazz = clazz;
        this.parseletMap = parseletMap;
        this.classConsumerMap = classConsumerMap;
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
    public void onMessage(T component, Object message) {
        Method consumer = classConsumerMap.get(message.getClass());
        if (consumer == null)
            Jvm.warn().on(getClass(), "Unable to find a consumer for " + message.getClass());
        else
            try {
                consumer.invoke(component, message);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Jvm.warn().on(getClass(), "Unable to invoke " + consumer + " " + message, e);
            }
    }

    @Override
    public Class<T> superInterface() {
        return clazz;
    }
}
