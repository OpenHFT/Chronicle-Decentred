package net.openhft.chronicle.decentred.dto;


import net.openhft.chronicle.decentred.api.ConnectionStatusListener;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.Verifier;

interface EndOfRoundBlockEventTester extends Verifier, MessageRouter<Verifier>, ConnectionStatusListener {
}
