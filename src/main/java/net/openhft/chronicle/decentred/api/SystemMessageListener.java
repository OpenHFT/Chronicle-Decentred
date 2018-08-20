package net.openhft.chronicle.decentred.api;


import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.dto.ApplicationErrorResponse;

public interface SystemMessageListener
        extends AccountManagementResponses, ConnectionStatusListener {

    /**
     * Notify an application error occurred in response to a message passed.
     *
     * @param applicationErrorResponse occurred
     */
    default void applicationError(ApplicationErrorResponse applicationErrorResponse) {
        Jvm.warn().on(getClass(), "Unhandled error " + applicationErrorResponse);
    }
}
