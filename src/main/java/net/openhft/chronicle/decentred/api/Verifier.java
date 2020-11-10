package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.address.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.VerificationEvent;

public interface Verifier {

    /**
     * Sends the provided signed {@code verificationEvent} message.
     *
     * @param verificationEvent to send
     */
    @MethodId(0xF100)
    void verificationEvent(VerificationEvent verificationEvent);

    /**
     * Sends the provided {@code invalidationEvent} notifying that
     * a server was invalidated.
     *
     * @param invalidationEvent to send
     */
    @MethodId(0xF101)
    void invalidationEvent(InvalidationEvent invalidationEvent);
}
