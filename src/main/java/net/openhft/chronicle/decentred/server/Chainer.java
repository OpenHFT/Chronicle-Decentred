package net.openhft.chronicle.decentred.server;


import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;

public interface Chainer extends MessageListener {
    TransactionBlockEvent nextTransactionBlockEvent();
}
