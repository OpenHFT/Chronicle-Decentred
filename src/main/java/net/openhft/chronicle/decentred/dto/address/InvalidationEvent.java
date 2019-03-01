package net.openhft.chronicle.decentred.dto.address;

import net.openhft.chronicle.decentred.dto.base.SelfSignedMessage;

/**
 * This message states this node verifies a given public key after connecting to it successfully.
 */
// Invalidate an address previously acquired by CreateAddressRequest
// Protect if the keys become known. Seal or archive an account.
// Currently not used
public final class InvalidationEvent extends SelfSignedMessage<InvalidationEvent> {}
