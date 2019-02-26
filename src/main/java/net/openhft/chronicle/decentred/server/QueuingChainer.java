package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.util.DtoParser;
import net.openhft.chronicle.decentred.util.DtoRegistry;

public class QueuingChainer implements MessageListener {
    private final Object transactionLock = new Object();
    private TransactionBlockEvent tbe = new TransactionBlockEvent();
    private TransactionBlockEvent tbe2 = new TransactionBlockEvent();


    public QueuingChainer(long chainAddress, DtoRegistry dtoRegistry) {
        DtoParser dtoParser = dtoRegistry.get();
        tbe.chainAddress(chainAddress).dtoParser(dtoParser);
        tbe2.chainAddress(chainAddress).dtoParser(dtoParser);
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
