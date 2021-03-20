package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.internal.server.QueuingChainer;
import net.openhft.chronicle.decentred.internal.server.VanillaChainer;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Chainer } is used to gather one or more {@link SignedMessage } objects and
 * collect them into a {@link TransactionBlockEvent}.
 *
 * Implementations of this interface should be thread safe.
 *
 * @param <T> the message union type
 *
 */
public interface Chainer<T> extends MessageListener {

    /**
     * Returns a {@link TransactionBlockEvent} consisting of
     * all messages previously received via the {@link #onMessage(SignedMessage)}
     * since this method was previously called.
     * <p>
     * If no {@link SignedMessage} was received since last call, this method will
     * return {@code null}.
     *
     * @return a {@link TransactionBlockEvent} consisting of
     *         all messages received via the {@link #onMessage(SignedMessage)} since
     *         this method was previously called
     */
    TransactionBlockEvent<T> nextTransactionBlockEvent();

    /**
     * Creates and returns a new standard {@link Chainer} for the provided {@code chainAddress}.
     *
     * @param chainAddress to use
     * @param dtoRegistry to use when handling messages
     * @param <T> message type
     * @return a new {@link Chainer} for the provided {@code chainAddress}
     */
    static <T> Chainer<T> createVanilla(long chainAddress, @NotNull DtoRegistry<T> dtoRegistry) {
        return new VanillaChainer<>(chainAddress, dtoRegistry);
    }

    /**
     * Creates and returns a new {@link Chainer} for the provided {@code chainAddress}.
     * <p>
     * The Chainer will queue incoming messages in a separate queue internally
     * before they are actually added to a TransactionBlockEvent.
     *
     * @param chainAddress to use
     * @param dtoRegistry to use when handling messages
     * @param <T> message type
     * @return a new {@link Chainer} for the provided {@code chainAddress}
     */
    static <T> Chainer<T> createQueuing(long chainAddress, @NotNull DtoRegistry<T> dtoRegistry) {
        return new QueuingChainer<>(chainAddress, dtoRegistry);
    }
}
