package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.dto.SignedMessage;
import net.openhft.chronicle.decentred.dto.TransactionBlockEvent;
import net.openhft.chronicle.decentred.util.DtoRegistry;

public class QueuingChainer implements MessageListener {
    private final Object transactionLock = new Object();
    private TransactionBlockEvent tbe = new TransactionBlockEvent();
    private TransactionBlockEvent tbe2 = new TransactionBlockEvent();


    public QueuingChainer(long chainAddress, DtoRegistry dtoRegistry) {
        tbe.chainAddress(chainAddress).dtoRegistry(dtoRegistry);
        tbe2.chainAddress(chainAddress).dtoRegistry(dtoRegistry);
    }

    @Override
    public void onMessage(SignedMessage message) {
        synchronized (transactionLock) {
            tbe.addTransaction(message);
        }
    }

    public TransactionBlockEvent nextTransactionBlockEvent() {
        TransactionBlockEvent tbeToSend;
        synchronized (transactionLock) {
//            System.out.println("tbe.isEmpty() " + tbe.isEmpty());
            if (tbe.isEmpty())
                return null;
            tbeToSend = tbe;
            tbe = tbe2;
            tbe2 = tbeToSend;
            tbe.reset();
        }
        return tbeToSend;
    }
}
