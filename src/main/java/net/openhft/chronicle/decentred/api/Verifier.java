package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.Invalidation;
import net.openhft.chronicle.decentred.dto.Verification;

public interface Verifier {
    /**
     * Called when a connection is first made
     */
    void onConnection();

    /**
     * Send a verification message which has been signed
     */
    @MethodId(1)
    void verification(Verification verification);

    /**
     * Notify that a server was invalidated
     */
    @MethodId(2)
    void invalidation(Invalidation record);
}
