package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.*;
import net.openhft.chronicle.decentred.dto.base.SelfSignedMessage;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.LongConversion;

/**
 * This message states this node verifies a given public key after connecting to it successfully.
 *
 */
@Deprecated
public class VerificationEvent extends SelfSignedMessage<VerificationEvent> {

    @LongConversion(AddressLongConverter.class)
    private long addressVerified;
    private Bytes keyVerified = Bytes.allocateElasticDirect(Ed25519.PUBLIC_KEY_LENGTH);

    public long addressVerified() {
        return addressVerified;
    }

    public VerificationEvent addressVerified(long addressVerified) {
        this.addressVerified = addressVerified;
        return this;
    }

    public VerificationEvent keyVerified(BytesStore key) {
        keyVerified.clear().write(key);
        addressVerified = DecentredUtil.toAddress(key);
        return this;
    }

    public BytesStore keyVerified() {
        return keyVerified;
    }

}
