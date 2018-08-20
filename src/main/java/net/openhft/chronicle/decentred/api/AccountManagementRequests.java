package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.CreateAccountRequest;

/**
 * Allows messages to be passed without needing to be self signing i.e. using just an account.
 */
public interface AccountManagementRequests {
    /**
     * Attempt to create an account.
     *
     * @param createAccountRequest to be processed
     */
    @MethodId(0x0100)
    void createAccountRequest(CreateAccountRequest createAccountRequest);
}
