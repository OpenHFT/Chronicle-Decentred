package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.util.ObjectUtils;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.DtoAliases;
import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static net.openhft.chronicle.decentred.util.DecentredUtil.MASK_16;

/**
 * This class is not thread safe although, once configured, the {@link #get()} method
 * can be invoked in a thread safe way.
 *
 * @param <T> the type of the super interface for all dto:s
 */
public final class VanillaDtoRegistry<T>  implements DtoRegistry<T> {
    static {
        DtoAliases.addAliases();
    }

    private final Class<T> superInterface;
    private final Map<Class, Integer> classToProtocolMessageType = new LinkedHashMap<>();
    private final IntObjMap<DtoParselet> parseletMap = IntObjMap.withExpectedSize(DtoParselet.class, 128);
    private final Map<Class, Method> classConsumerMap = new LinkedHashMap<>();

    VanillaDtoRegistry(Class<T> superInterface) {
        this.superInterface = requireNonNull(superInterface);
        addProtocol(0xFFFF, (Class) SystemMessages.class); // Todo: T or super type?
    }
    @Override
    public VanillaDtoRegistry<T> addProtocol(int protocol, Class<? super T> pClass) {
        if (protocol < 0) {
            throw new IllegalArgumentException("protocol cannot be negative:" + protocol);
        }
        requireNonNull(pClass);
        for (Method method : pClass.getMethods()) {
            final MethodId mid = method.getAnnotation(MethodId.class);
            if (mid != null) {
                assert (mid.value() | MASK_16) == MASK_16;
                final int key = (int) ((protocol << 16) + mid.value());
                try {
                    parseletMap.justPut(key,
                            new DtoParselet(method, protocol, Maths.toUInt16(mid.value())));
                    final Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != 1) {
                        Jvm.warn().on(getClass(), "Methods must have 1 parameter " + method);
                        continue;
                    }
                    final Class<?> parameterType = parameterTypes[0];
                    classToProtocolMessageType.put(parameterType, key);
                    classConsumerMap.putIfAbsent(parameterType, method);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return this;
    }

    @Override
    public int protocolFor(Class clazz) {
        return protocolMessageTypeFor(clazz) >>> 16;
    }

    @Override
    public int messageTypeFor(Class clazz) {
        return protocolMessageTypeFor(clazz) & MASK_16;
    }

    @Override
    public int protocolMessageTypeFor(Class clazz) {
        requireNonNull(clazz);
        final Integer pmt = classToProtocolMessageType.get(clazz);
        if (pmt == null) {
            throw new IllegalStateException(clazz + " not defined");
        }
        return pmt;
    }

    @Override
    public DtoParser<T> get() {
        final IntObjMap<DtoParselet> parseletMap2 = IntObjMap.withExpectedSize(DtoParselet.class, parseletMap.size() * 2);
        parseletMap.forEach((i, dp) -> parseletMap2.justPut(i, new DtoParselet(dp)));
        return new VanillaDtoParser<>(superInterface, parseletMap2, classConsumerMap);
    }

    @Override
    public <M extends VanillaSignedMessage<M>> M create(Class<M> tClass) {
        requireNonNull(tClass);
        final int pmt = protocolMessageTypeFor(tClass);
        try {
            final int protocol = pmt >>> 16;
            final int messageType = pmt & MASK_16;
            final M vsm = ObjectUtils.newInstance(tClass);
            return vsm.protocol(protocol).messageType(messageType);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public Class<T> superInterface() {
        return superInterface;
    }
}
