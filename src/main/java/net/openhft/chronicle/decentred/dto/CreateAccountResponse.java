package net.openhft.chronicle.decentred.dto;

public class CreateAccountResponse extends VanillaSignedMessage {
    private CreateAccountRequest createAccountRequest;

    public CreateAccountResponse(int protocol, int messageType) {
        super(protocol, messageType);
    }

    public CreateAccountRequest createAccount() {
        return createAccountRequest;
    }

    public CreateAccountResponse createAccount(CreateAccountRequest createAccountRequest) {
        assert !signed();
        this.createAccountRequest = createAccountRequest;
        return this;
    }
}
