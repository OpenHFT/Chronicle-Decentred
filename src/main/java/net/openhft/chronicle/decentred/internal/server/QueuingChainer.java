package net.openhft.chronicle.decentred.internal.server;

import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.server.Chainer;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class QueuingChainer<T> implements Chainer<T> {

    private final List<SignedMessage> messages = new ArrayList<>();
    private final long chainAddress;
    private final DtoRegistry<T> dtoRegistry;

    public QueuingChainer(long chainAddress, @NotNull DtoRegistry<T> dtoRegistry) {
        this.dtoRegistry = dtoRegistry;
        this.chainAddress = chainAddress;
    }

    @Override
    public void onMessage(SignedMessage message) {
        synchronized (messages) {
            messages.add(message);
        }
    }

    public TransactionBlockEvent<T> nextTransactionBlockEvent() {
        synchronized (messages) {
            if (messages.isEmpty()) {
                return null;
            }

            @SuppressWarnings("unchecked")
            final TransactionBlockEvent<T> tbe = ((TransactionBlockEvent<T>) dtoRegistry.create(TransactionBlockEvent.class))
                .chainAddress(chainAddress)
                .dtoParser(dtoRegistry.get());

            messages.forEach(tbe::addTransaction);
            messages.clear();
            return tbe;
        }
    }
}
