package net.openhft.chronicle.decentred.dto;

/**
 * This message states this node verifies a given public key after connecting to it successfully.
 */
public class InvalidationEvent extends SelfSignedMessage<InvalidationEvent> {

    public InvalidationEvent() {
    }

    public InvalidationEvent(int protocol, int messageType) {
        super(protocol, messageType);
    }

}
