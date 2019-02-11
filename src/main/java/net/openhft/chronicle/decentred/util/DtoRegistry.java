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
import java.util.function.Supplier;

import static net.openhft.chronicle.decentred.util.DecentredUtil.MASK_16;

/**
 * A holder of the different protocols that are available for all data transfer objects (dto:s) and
 * can supply new {@link DtoParser} objects.
 *
 * @param <T> the type of the super interface for all dto:s
 */
public class DtoRegistry<T> implements Supplier<DtoParser<T>>, HasSuperInterface<T> {
    static {
        DtoAliases.addAliases();
    }

    private final Class<T> superInterface;
    private final Map<Class, Integer> classToProtocolMessageType = new LinkedHashMap<>();
    private final IntObjMap<DtoParselet> parseletMap = IntObjMap.withExpectedSize(DtoParselet.class, 128);
    private final Map<Class, Method> classConsumerMap = new LinkedHashMap<>();

    private DtoRegistry(Class<T> superInterface) {
        this.superInterface = superInterface;
        addProtocol(0xFFFF, (Class) SystemMessages.class);
    }

    /**
     * Creates and returns a new {@link DtoRegistry}.
     * @param superInterface for all dto:s
     * @param <T> super interface type
     * @return a new {@link DtoRegistry}
     */
    public static <T> DtoRegistry<T> newRegistry(Class<T> superInterface) {
        return new DtoRegistry<>(superInterface);
    }

    public static <T> DtoRegistry<T> newRegistry(int protocol, Class<T> superInterface) {
        return new DtoRegistry<>(superInterface).addProtocol(protocol, superInterface);
    }

    /**
     * Adds a new protocol for this registry with the provided protocol
     * number {@code protocol} and provided protocol class {@code pClass}.
     * <p>
     * The given {@code protocol} will be associated with the given {@code pClass}.
     * <p>
     * The provided {@code pClass} objects must contain at least one method
     * annotated with the {@link net.openhft.chronicle.bytes.MethodId} annotation
     * or this method will have no effect.
     *
     * @param protocol number to use
     * @param pClass protocol class to be associated
     * @return this (potentially modified) DtoRegistry
     *
     * @throws IllegalArgumentException if the provided {@code protocol} is negative or
     * if the protocol otherwise cannot be added to this registry.
     * @throws NullPointerException if the provided {@code pClass} is {@code null}
     */
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

    /**
     * Returns the protocol number for the provided message parameter {@code clazz}.
     *
     * @param clazz of the method parameter to use for retrieval.
     * @return the protocol number for the provided message parameter {@code clazz}
     *
     * @throws NullPointerException if the provided {@code clazz} is {@code null}
     */
    public int protocolFor(Class clazz) {
        return protocolMessageTypeFor(clazz) >>> 16;
    }

    /**
     * Returns the message type which is the {@link net.openhft.chronicle.bytes.MethodId}
     * value for the provided message parameter {@code clazz}.
     *
     * @param clazz of the method parameter to use for retrieval.
     * @return the message type which is the {@link net.openhft.chronicle.bytes.MethodId}
     *         value for the provided message parameter {@code clazz}
     *
     * @throws NullPointerException if the provided {@code clazz} is {@code null}
     */
    public int messageTypeFor(Class clazz) {
        return protocolMessageTypeFor(clazz) & MASK_16;
    }

    /**
     * Returns the combined protocol number and message type
     * (i.e. {@link net.openhft.chronicle.bytes.MethodId} value) for the provided
     * message parameter {@code clazz}.
     *
     * @param clazz of the method parameter to use for retrieval.
     * @return the combined protocol number and message type
     *         (i.e. {@link net.openhft.chronicle.bytes.MethodId} value) for the provided
     *         message parameter {@code clazz}
     *
     * @throws NullPointerException if the provided {@code clazz} is {@code null}
     */
    public int protocolMessageTypeFor(Class clazz) {
        Integer pmt = classToProtocolMessageType.get(clazz);
        if (pmt == null) throw new IllegalStateException(clazz + " not defined");
        return pmt;
    }

    /**
     * Creates and returns a new DtoParser.
     *
     * @return a new DtoParser
     */
    @Override
    public DtoParser<T> get() {
        IntObjMap<DtoParselet> parseletMap2 = IntObjMap.withExpectedSize(DtoParselet.class, parseletMap.size() * 2);
        parseletMap.forEach((i, dp) -> parseletMap2.justPut(i, new DtoParselet(dp)));
        return new VanillaDtoParser<>(superInterface, parseletMap2, classConsumerMap);
    }

    /**
     * Creates and returns a new VanillaSignedMessage of the provided {@code tClass} type.
     *
     * @param tClass to use when creating a new message
     * @param <M>    Message type
     * @return       a new VanillaSignedMessage of the provided {@code tClass} type
     *
     * @throws NullPointerException if the provided {@code tClass } is {@code null}
     */
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
