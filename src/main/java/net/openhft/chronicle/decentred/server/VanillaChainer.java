package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;

public class VanillaChainer implements Chainer {
    private final Object transactionLock = new Object();
    private TransactionBlockEvent tbe;
    private TransactionBlockEvent tbe2;

    public VanillaChainer(long chainAddress) {
        tbe = new TransactionBlockEvent().chainAddress(chainAddress);
        tbe2 = new TransactionBlockEvent().chainAddress(chainAddress);
    }

    @Override
    public void onMessage(SignedMessage message) {
        synchronized (transactionLock) {
//            System.out.println("Add "+message);
            tbe.addTransaction(message);
        }
    }

    public TransactionBlockEvent nextTransactionBlockEvent() {
        TransactionBlockEvent tbeToSend;
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
}
