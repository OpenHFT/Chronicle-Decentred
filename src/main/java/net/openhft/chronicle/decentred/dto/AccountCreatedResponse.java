package net.openhft.chronicle.decentred.dto;

public class AccountCreatedResponse extends VanillaSignedMessage {
    private CreateAccountCommand createAccountCommand;

    public AccountCreatedResponse(int protocol, int messageType) {
        super(protocol, messageType);
    }

    public CreateAccountCommand createAccount() {
        return createAccountCommand;
    }

    public AccountCreatedResponse createAccount(CreateAccountCommand createAccountCommand) {
        assert !signed();
        this.createAccountCommand = createAccountCommand;
        return this;
    }
}
