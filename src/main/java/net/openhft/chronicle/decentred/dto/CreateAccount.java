package net.openhft.chronicle.decentred.dto;

public class CreateAccount extends SelfSignedMessage<CreateAccount> {

    public CreateAccount(int protocol, int messageType) {
        super(protocol, messageType);
    }
}
