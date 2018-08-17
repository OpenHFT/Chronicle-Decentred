package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.VerificationEvent;

public interface Verifier {
    /**
     * Called when a connection is first made
     */
    void onConnection();

    /**
     * Send a verificationEvent message which has been signed
     */
    @MethodId(1)
    void verificationEvent(VerificationEvent verificationEvent);

    /**
     * Notify that a server was invalidated
     */
    @MethodId(2)
    void invalidationEvent(InvalidationEvent record);
}
