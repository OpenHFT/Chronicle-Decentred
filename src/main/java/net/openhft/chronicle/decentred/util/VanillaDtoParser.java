package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

final class VanillaDtoParser<T> implements DtoParser<T> {
    private final Class<T> clazz;

    private final IntObjMap<DtoParselet> parseletMap;
    private final Map<Class, Method> classConsumerMap;

    VanillaDtoParser(@NotNull Class<T> clazz, @NotNull IntObjMap<DtoParselet> parseletMap, @NotNull Map<Class, Method> classConsumerMap) {
        this.clazz = clazz;
        this.parseletMap = parseletMap;
        this.classConsumerMap = classConsumerMap;
        /*this.parseletMap = IntObjMap.withExpectedSize(DtoParselet.class, parseletMap.size() * 2);
        parseletMap.forEach((i, dp) -> this.parseletMap.justPut(i, new DtoParselet(dp)));
        this.classConsumerMap = new HashMap<>(classConsumerMap);*/
    }

    @Override
    public void parseOne(@NotNull BytesIn bytes, @NotNull T listener) {
        final long start = bytes.readPosition();
        final int protocolMessageType = bytes.readInt(start + VanillaSignedMessage.MESSAGE_TYPE);
        final DtoParselet parselet = parseletMap.get(protocolMessageType);
        // System.out.println("Incoming message for protocol " + (protocolMessageType >>> 16) + " messageType " + Integer.toHexString(protocolMessageType & 0xFFFF));
        if (bytes.readPosition() >= bytes.readLimit()) {
            throw new IllegalStateException();
        }
        if (parselet == null) {
            warnNotFound(protocolMessageType);
        } else {
            parselet.parse(bytes, listener);
        }
    }

    @Override
    public SignedMessage parseOne(@NotNull BytesIn bytes) {
        final int protocolMessageType = bytes.readInt(bytes.readPosition() + VanillaSignedMessage.MESSAGE_TYPE);
        final DtoParselet parselet = parseletMap.get(protocolMessageType);
        // System.out.println("Parsed message for protocol " + (protocolMessageType >>> 16) + " messageType " + Integer.toHexString(protocolMessageType & 0xFFFF));
        if (parselet == null) {
            warnNotFound(protocolMessageType);
            return null;
        } else {
            return (SignedMessage) parselet.parse(bytes);
        }
    }

    @Override
    public void onMessage(@NotNull T component, @NotNull Object message) {
        final Method consumer = classConsumerMap.get(message.getClass());
        if (consumer == null) {
            Jvm.warn().on(getClass(), "Unable to find a consumer for " + message.getClass());
        } else {
            try {
                consumer.invoke(component, message);
            } catch (ReflectiveOperationException e) {
                Jvm.warn().on(getClass(), "Unable to invoke " + consumer + " " + message, e);
            }
        }
    }

    @Override
    public Class<T> superInterface() {
        return clazz;
    }

    private void warnNotFound(int protocolMessageType) {
        final int protocol = (protocolMessageType >>> 16);
        final int messageType =  (protocolMessageType & 0xFFFF);
        Jvm.warn().on(getClass(), String.format("Unable to find a parselet for protocol %d messageType %d (0x%04x)", protocol, messageType, messageType));
    }

}
