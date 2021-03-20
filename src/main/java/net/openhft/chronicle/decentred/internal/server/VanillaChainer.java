package net.openhft.chronicle.decentred.internal.server;

import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.server.Chainer;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import org.jetbrains.annotations.NotNull;

public final class VanillaChainer<T> implements Chainer<T> {

    private final Object transactionLock = new Object();
    private TransactionBlockEvent tbe;
    private TransactionBlockEvent tbe2;

    public VanillaChainer(long chainAddress, @NotNull DtoRegistry<T> dtoRegistry) {
        tbe = create(chainAddress, dtoRegistry);
        tbe2 = create(chainAddress, dtoRegistry);
    }

    @Override
    public void onMessage(SignedMessage message) {
        synchronized (transactionLock) {
//            System.out.println("Add "+message);
            tbe.addTransaction(message);
        }
    }

    public TransactionBlockEvent<T> nextTransactionBlockEvent() {
        final TransactionBlockEvent tbeToSend;
        synchronized (transactionLock) {
//            System.out.println(System.currentTimeMillis()+"  TBE count "+tbe.count());
            if (tbe.isEmpty())
                return null;
            tbeToSend = tbe;
            tbe = tbe2;
            tbe2 = tbeToSend;
            tbe.reset();
        }
        return tbeToSend;
    }

    private TransactionBlockEvent<T> create(long chainAddress, @NotNull DtoRegistry<T> dtoRegistry) {
        @SuppressWarnings("unchecked") final TransactionBlockEvent<T> tbe = ((TransactionBlockEvent<T>) dtoRegistry.create(TransactionBlockEvent.class))
            .chainAddress(chainAddress)
            .dtoParser(dtoRegistry.get());
        return tbe;
    }
}
