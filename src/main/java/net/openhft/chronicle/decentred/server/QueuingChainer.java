package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.util.DtoRegistry;

import java.util.ArrayList;
import java.util.List;

public class QueuingChainer<T> implements MessageListener {
    private final List<SignedMessage> messages = new ArrayList<>();
    private final long chainAddress;
    private final DtoRegistry<T> dtoRegistry;

    public QueuingChainer(long chainAddress, DtoRegistry<T> dtoRegistry) {
        this.dtoRegistry = dtoRegistry;
        this.chainAddress = chainAddress;
    }

    @Override
    public void onMessage(SignedMessage message) {
        synchronized (messages) {
            messages.add(message);
        }
    }

    public TransactionBlockEvent<T> createTransactionBlockEvent() {
        synchronized (messages) {
            if (messages.isEmpty())
                return null;

            TransactionBlockEvent<T> tbe = ((TransactionBlockEvent<T>)dtoRegistry.create(TransactionBlockEvent.class))
                .chainAddress(chainAddress)
                .dtoParser(dtoRegistry.get());
            messages.forEach(tbe::addTransaction);
            messages.clear();
            return tbe;
        }
    }
}
