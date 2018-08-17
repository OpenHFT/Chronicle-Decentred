package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.decentred.dto.AccountCreatedResponse;
import net.openhft.chronicle.decentred.dto.CreateAccountCommand;

/**
 * Allows messages to be passed without needing to be self signing i.e. using just an account.
 */
public interface AccountManagementListener {
    /**
     * Attempt to create an account.
     *
     * @param createAccountCommand to be processed
     */
    void createAccountCommand(CreateAccountCommand createAccountCommand);

    /**
     * Notifiy that an account was created.
     *
     * @param accountCreatedResponse record
     */
    void accountCreatedResponse(AccountCreatedResponse accountCreatedResponse);

}
