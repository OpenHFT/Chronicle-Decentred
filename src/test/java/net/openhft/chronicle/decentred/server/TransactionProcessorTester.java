package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.SystemMessages;

public interface TransactionProcessorTester extends MessageRouter<SystemMessages>, SystemMessages {
}
