package net.openhft.chronicle.decentred.dto;

public class CreateAccountRequest extends SelfSignedMessage<CreateAccountRequest> {

    public CreateAccountRequest(int protocol, int messageType) {
        super(protocol, messageType);
    }
}
