package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.VerificationEvent;

public interface Verifier {
    /**
     * Send a verificationEvent message which has been signed
     */
    @MethodId(0xF100)
    void verificationEvent(VerificationEvent verificationEvent);

    /**
     * Notify that a server was invalidated
     */
    @MethodId(0xF101)
    void invalidationEvent(InvalidationEvent invalidationEvent);
}
