package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.decentred.dto.CreateAccountRequest;
import net.openhft.chronicle.decentred.dto.CreateAccountResponse;

/**
 * Allows messages to be passed without needing to be self signing i.e. using just an account.
 */
public interface AccountManagementListener {
    /**
     * Attempt to create an account.
     *
     * @param createAccountRequest to be processed
     */
    void createAccountRequest(CreateAccountRequest createAccountRequest);

    /**
     * Notifiy that an account was created.
     *
     * @param createAccountResponse record
     */
    void createAccountResponse(CreateAccountResponse createAccountResponse);

}
