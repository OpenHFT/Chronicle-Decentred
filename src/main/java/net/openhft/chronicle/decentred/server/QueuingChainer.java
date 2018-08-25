package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.dto.SignedMessage;
import net.openhft.chronicle.decentred.dto.TransactionBlockEvent;

import java.util.LinkedList;
import java.util.Queue;

public class QueuingChainer implements MessageListener {
    private final Object transactionLock = new Object();
    private TransactionBlockEvent tbe = new TransactionBlockEvent();
    private TransactionBlockEvent tbe2 = new TransactionBlockEvent();
    private Queue<TransactionBlockEvent> queue = new LinkedList<>();

    private long chainAddress;

    public QueuingChainer(long chainAddress) {
        tbe.chainAddress(chainAddress);
        tbe2.chainAddress(chainAddress);
        this.chainAddress = chainAddress;
    }

    @Override
    public void onMessage(SignedMessage message) {
        synchronized (transactionLock) {
            if (tbe.isBufferFull()) {
                System.out.println("buffer is full - creating new block");
                this.queue.add(tbe);
                tbe = new TransactionBlockEvent();
                tbe.chainAddress(chainAddress);
            }
            tbe.addTransaction(message);
        }
    }

    public TransactionBlockEvent nextTransactionBlockEvent() {
        TransactionBlockEvent tbeToSend;
        String name = Thread.currentThread().getName();
        synchronized (transactionLock) {
            if (queue.size() > 0)
                System.out.println(name + " - tbe's queue size = " + queue.size());
            if (!queue.isEmpty()) {
                return queue.poll();
            }

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
