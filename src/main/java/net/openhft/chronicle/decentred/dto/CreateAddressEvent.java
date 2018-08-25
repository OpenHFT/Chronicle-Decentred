package net.openhft.chronicle.decentred.dto;

public class CreateAddressEvent extends VanillaSignedMessage<CreateAddressEvent> {
    private CreateAddressRequest createAddressRequest;

    public CreateAddressRequest createAccountRequest() {
        return createAddressRequest;
    }

    public CreateAddressEvent createAccountRequest(CreateAddressRequest createAddressRequest) {
        assert !signed();
        this.createAddressRequest = createAddressRequest;
        return this;
    }
}
