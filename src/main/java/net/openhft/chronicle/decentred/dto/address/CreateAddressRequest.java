package net.openhft.chronicle.decentred.dto.address;

import net.openhft.chronicle.decentred.dto.base.SelfSignedMessage;

// Test this
// Is more of a Linux account
// Associates a long with a PublicKey (=put(long, publickey)
// Todo: Prevent reuse of public key
// It tests we would like to turn of verification to test same long...
public final class CreateAddressRequest extends SelfSignedMessage<CreateAddressRequest> {}
