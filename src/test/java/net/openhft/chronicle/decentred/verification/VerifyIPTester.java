package net.openhft.chronicle.decentred.verification;


import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.Verifier;

public interface VerifyIPTester extends Verifier, MessageRouter<Verifier> {
}
