package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;

import java.util.function.Supplier;

/**
 * A holder of the different protocols that are available for all data transfer objects (dto:s) and
 * can supply new {@link DtoParser} objects.
 *
 * @param <T> the type of the super interface for all dto:s
 */
public interface DtoRegistry<T> extends Supplier<DtoParser<T>>, HasSuperInterface<T> {
    /**
     * Creates and returns a new {@link DtoRegistry}.
     * @param superInterface for all dto:s
     * @param <T> super interface type
     * @return a new {@link DtoRegistry}
     */
    static <T> DtoRegistry<T> newRegistry(Class<T> superInterface) {
        return new VanillaDtoRegistry<>(superInterface);
    }

    static <T> DtoRegistry<T> newRegistry(int protocol, Class<T> superInterface) {
        return new VanillaDtoRegistry<>(superInterface).addProtocol(protocol, superInterface);
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
    DtoRegistry<T> addProtocol(int protocol, Class<? super T> pClass);

    /**
     * Returns the protocol number for the provided message parameter {@code clazz}.
     *
     * @param clazz of the method parameter to use for retrieval.
     * @return the protocol number for the provided message parameter {@code clazz}
     *
     * @throws NullPointerException if the provided {@code clazz} is {@code null}
     */
    int protocolFor(Class clazz);

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
    int messageTypeFor(Class clazz);
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
    int protocolMessageTypeFor(Class clazz);

    /**
     * Creates and returns a new DtoParser.
     *
     * @return a new DtoParser
     */
    @Override
    DtoParser<T> get();

    <K> DtoParser<K> get(Class<K> token);

    /**
     * Creates and returns a new VanillaSignedMessage of the provided {@code tClass} type.
     *
     * @param tClass to use when creating a new message
     * @param <M>    Message type
     * @return       a new VanillaSignedMessage of the provided {@code tClass} type
     *
     * @throws NullPointerException if the provided {@code tClass } is {@code null}
     */
    <M extends VanillaSignedMessage<M>> M create(Class<M> tClass);

}
