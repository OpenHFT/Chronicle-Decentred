package net.openhft.chronicle.decentred.dto;

/**
 * This message states this node verifies a given public key after connecting to it successfully.
 */
public class InvalidationCommand extends SelfSignedMessage<InvalidationCommand> {

    public InvalidationCommand() {
    }

    public InvalidationCommand(int protocol, int messageType) {
        super(protocol, messageType);
    }

}
