package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.salt.Ed25519;

/**
 * This message states this node verifies a given public key after connecting to it successfully.
 */
public class SelfSignedMessage<M extends SelfSignedMessage<M>> extends VanillaSignedMessage<M> {
    private Bytes publicKey = Bytes.allocateElasticDirect(Ed25519.PUBLIC_KEY_LENGTH);

    public SelfSignedMessage() {
    }

    @Override
    public boolean hasPublicKey() {
        return true;
    }

    @Override
    public M publicKey(BytesStore key) {
        assert !signed();
        long offset = key.readLimit() - Ed25519.PUBLIC_KEY_LENGTH;
        this.publicKey.clear().write(key, offset, Ed25519.PUBLIC_KEY_LENGTH);
        return (M) this;
    }

    public BytesStore publicKey() {
        return publicKey;
    }
/*
    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        super.readMarshallable(wire);
        if (publicKey == null)
            publicKey = Bytes.allocateElasticDirect(Ed25519.PUBLIC_KEY_LENGTH);
        wire.read("publicKey").bytes(publicKey);
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        super.writeMarshallable(wire);
        wire.write("publicKey").bytes(publicKey);
    }

    @Override
    protected void readMarshallable0(BytesIn bytes) {
        super.readMarshallable0(bytes);
        int length = Ed25519.PUBLIC_KEY_LENGTH;
        if (publicKey == null)
            publicKey = Bytes.allocateElasticDirect(length);
        publicKey.write(bytes, bytes.readPosition(), length);
        bytes.readSkip(length);
    }

    @Override
    protected void writeMarshallable0(BytesOut bytes) {
        super.writeMarshallable0(bytes);
        assert publicKey.readRemaining() == Ed25519.PUBLIC_KEY_LENGTH;
        bytes.write(publicKey);
    }*/
}
