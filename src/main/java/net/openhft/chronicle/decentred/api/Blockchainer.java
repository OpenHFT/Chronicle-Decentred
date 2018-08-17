package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.decentred.dto.TransactionBlockEvent;

public interface Blockchainer {
    void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent);
}
