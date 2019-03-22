package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.decentred.dto.SignedMessage;

public interface DtoParser<T>
    extends HasSuperInterface<T> {

    /**
     * Parses one data transfer object (dto) from the provided {@code bytes} and
     * subsequently invokes the provided {@code listener} using the parsed
     * dto.
     * <p>
     * The read position of the provided {@code bytes} is not affected.
     * <p>
     * If the method is unable to parse a dto, the listener is never invoked
     *
     * @param bytes to use for parsing the dto
     * @param listener to invoke once the dto is parsed
     *
     * @throws NullPointerException if any of the provided parameters
     * are {@code null}
     *
     */
    void parseOne(BytesIn bytes, T listener);

    SignedMessage parseOne(BytesIn bytes);

    /**
     * Invokes a method on the given {@code component} with the given {@code message}
     * as an argument. The invocation method is determined using the class of the
     * message.
     * <p>
     * If the method cannot be found or if the invocation fails, the method
     * is never invoked.
     *
     * @param component on which to invoke the method
     * @param message to use as a parameter to the component method invocation
     *
     * @throws NullPointerException if any of the provided parameters
     * are {@code null}
     */
    void onMessage(T component, Object message);
}
