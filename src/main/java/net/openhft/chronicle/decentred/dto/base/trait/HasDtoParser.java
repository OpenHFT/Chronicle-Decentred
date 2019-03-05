package net.openhft.chronicle.decentred.dto.base.trait;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.DtoParser;
import org.jetbrains.annotations.NotNull;

/**
 * Signifies that this message has a {@link DtoParser}.
 *
 * @param <T> message type
 * @param <U> DtoParser union messages type
 */
public interface HasDtoParser<T extends VanillaSignedMessage<T>, U> {

    /**
     * Returns the {@link DtoParser} for this message.
     *
     * @return the {@link DtoParser} for this message
     */
    DtoParser<U> dtoParser();

    /**
     * Sets the {@link DtoParser} for this message.
     *
     * @param dtoParser to use for setting
     * @return this instance
     *
     * @throws NullPointerException if the provided {@code dtoParser}
     * is {@code null}
     */
    T dtoParser(@NotNull DtoParser<U> dtoParser);

}
