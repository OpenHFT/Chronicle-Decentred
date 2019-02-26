package net.openhft.chronicle.decentred.api;


import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.dto.error.ApplicationErrorResponse;

public interface SystemMessageListener extends
        AddressManagementResponses,
        Verifier,
        ConnectionStatusListener {

    /**
     * Notify an application error occurred in response to a message passed.
     *
     * @param applicationErrorResponse occurred
     */
    @MethodId(0xFF10)
    default void applicationError(ApplicationErrorResponse applicationErrorResponse) {
        Jvm.warn().on(getClass(), "Unhandled error " + applicationErrorResponse);
    }
}
