package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.dto.CreateAccountResponse;

/**
 * Allows messages to be passed without needing to be self signing i.e. using just an account.
 */
public interface AccountManagementResponses {
    /**
     * Notify that an account was created.
     *
     * @param createAccountResponse record
     */
    default void createAccountResponse(CreateAccountResponse createAccountResponse) {
        Jvm.debug().on(getClass(), "Account created " + createAccountResponse);
    }
}
