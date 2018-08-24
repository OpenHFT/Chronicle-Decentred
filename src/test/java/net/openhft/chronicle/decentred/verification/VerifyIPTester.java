package net.openhft.chronicle.decentred.verification;


import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.Verifier;

interface VerifyIPTester extends Verifier, MessageRouter<Verifier> {
}
