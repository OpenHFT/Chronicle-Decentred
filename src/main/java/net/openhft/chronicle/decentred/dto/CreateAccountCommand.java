package net.openhft.chronicle.decentred.dto;

public class CreateAccountCommand extends SelfSignedMessage<CreateAccountCommand> {

    public CreateAccountCommand(int protocol, int messageType) {
        super(protocol, messageType);
    }
}
