package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.CreateAddressRequest;

/**
 * Allows messages to be passed without needing to be self signing i.e. using just an account.
 */
public interface AccountManagementRequests {
    /**
     * Attempt to create an account.
     *
     * @param createAddressRequest to be processed
     */
    @MethodId(0xF000)
    void createAccountRequest(CreateAddressRequest createAddressRequest);
}
