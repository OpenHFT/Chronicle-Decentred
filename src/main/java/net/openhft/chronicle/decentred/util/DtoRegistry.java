package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.util.ObjectUtils;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.openhft.chronicle.decentred.util.DecentredUtil.MASK_16;

public class DtoRegistry<T> implements Supplier<DtoParser<T>> {

    private final Class<T> superInterface;
    private final Map<Class, Integer> classToProtocolMessageType = new LinkedHashMap<>();
    private final IntObjMap<DtoParselet> parseletMap = IntObjMap.withExpectedSize(DtoParselet.class, 128);
    private final Map<Class, Method> classConsumerMap = new LinkedHashMap<>();

    private DtoRegistry(Class<T> superInterface) {
        this.superInterface = superInterface;
        addProtocol(0xFFFF, (Class) SystemMessages.class);
    }

    public static <T> DtoRegistry<T> newRegistry(Class<T> superInterface) {
        return new DtoRegistry<>(superInterface);
    }

    public static <T> DtoRegistry<T> newRegistry(int protocol, Class<T> superInterface) {
        return new DtoRegistry<>(superInterface).addProtocol(protocol, superInterface);
    }

    public DtoRegistry<T> addProtocol(int protocol, Class<? super T> pClass) {
        for (Method method : pClass.getMethods()) {
            MethodId mid = method.getAnnotation(MethodId.class);
            if (mid != null) {
                assert (mid.value() | MASK_16) == MASK_16;
                int key = (int) ((protocol << 16) + mid.value());
                try {
                    parseletMap.justPut(key,
                            new DtoParselet(method, protocol, Maths.toUInt16(mid.value())));
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != 1) {
                        Jvm.warn().on(getClass(), "Methods must have 1 parameter " + method);
                        continue;
                    }
                    Class<?> parameterType = parameterTypes[0];
                    classToProtocolMessageType.put(parameterType, key);
                    classConsumerMap.putIfAbsent(parameterType, method);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return this;
    }

    public int protocolFor(Class clazz) {
        return protocolMessageTypeFor(clazz) >>> 16;
    }

    public int messageTypeFor(Class clazz) {
        return protocolMessageTypeFor(clazz) & MASK_16;
    }

    public int protocolMessageTypeFor(Class clazz) {
        Integer pmt = classToProtocolMessageType.get(clazz);
        if (pmt == null) throw new IllegalStateException(clazz + " not defined");
        return pmt;
    }

    @Override
    public DtoParser<T> get() {
        IntObjMap<DtoParselet> parseletMap2 = IntObjMap.withExpectedSize(DtoParselet.class, parseletMap.size() * 2);
        parseletMap.forEach((i, dp) -> parseletMap2.justPut(i, new DtoParselet(dp)));
        return new VanillaDtoParser<>(superInterface, parseletMap2, classConsumerMap);
    }

    public <M extends VanillaSignedMessage<M>> M create(Class<M> tClass) {
        int pmt = protocolMessageTypeFor(tClass);
        try {
            int protocol = pmt >>> 16;
            int messageType = pmt & MASK_16;
            M vsm = ObjectUtils.newInstance(tClass);
            return vsm.protocol(protocol).messageType(messageType);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public Class<T> superInterface() {
        return superInterface;
    }
}
