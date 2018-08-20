package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.salt.Ed25519;
import org.jetbrains.annotations.NotNull;

/**
 * This message states this node verifies a given public key after connecting to it successfully.
 */
public class VerificationEvent extends SelfSignedMessage<VerificationEvent> {
    private final Bytes keyVerified = Bytes.allocateElasticDirect(Ed25519.PUBLIC_KEY_LENGTH);

    public VerificationEvent keyVerified(BytesStore key) {
        keyVerified.clear().write(key);
        return this;
    }

    public BytesStore keyVerified() {
        return keyVerified;
    }

    @NotNull
    @Override
    public <T> T deepCopy() {
        VerificationEvent v2 = new VerificationEvent();
        v2.protocol(protocol());
        v2.messageType(messageType());
        v2.address(address());
        v2.timestampUS(timestampUS());
        v2.publicKey(publicKey());
        v2.keyVerified(keyVerified());
        return (T) v2;
    }
}
