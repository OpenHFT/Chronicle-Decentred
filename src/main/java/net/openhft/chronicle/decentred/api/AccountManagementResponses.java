package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.dto.CreateAddressEvent;

/**
 * Allows messages to be passed without needing to be self signing i.e. using just an account.
 */
public interface AccountManagementResponses {
    /**
     * Notify that an account was created.
     *
     * @param createAddressEvent record
     */
    @MethodId(0xF080)
    default void createAddressEvent(CreateAddressEvent createAddressEvent) {
        Jvm.debug().on(getClass(), "Account created " + createAddressEvent);
    }
}
