package net.openhft.chronicle.decentred.api;


import net.openhft.chronicle.decentred.dto.ApplicationErrorResponse;

public interface SystemMessageListener
        extends AccountManagementListener, ConnectionStatusListener {

    /**
     * Notify an application error occurred in response to a message passed.
     *
     * @param applicationErrorResponse occurred
     */
    void applicationError(ApplicationErrorResponse applicationErrorResponse);
}
