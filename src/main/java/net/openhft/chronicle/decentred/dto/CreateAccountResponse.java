package net.openhft.chronicle.decentred.dto;

public class CreateAccountResponse extends VanillaSignedMessage<CreateAccountResponse> {
    private CreateAccountRequest createAccountRequest;

    public CreateAccountRequest createAccountRequest() {
        return createAccountRequest;
    }

    public CreateAccountResponse createAccountRequest(CreateAccountRequest createAccountRequest) {
        assert !signed();
        this.createAccountRequest = createAccountRequest;
        return this;
    }
}
