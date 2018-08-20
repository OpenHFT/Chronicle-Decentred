package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.TransactionBlockEvent;

public interface Blockchainer {
    @MethodId(0x200)
    void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent);
}
