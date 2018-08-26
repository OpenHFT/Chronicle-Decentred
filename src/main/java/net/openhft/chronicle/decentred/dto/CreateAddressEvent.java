package net.openhft.chronicle.decentred.dto;

public class CreateAddressEvent extends VanillaSignedMessage<CreateAddressEvent> {
    private CreateAddressRequest createAddressRequest;

    public CreateAddressRequest createAddressRequest() {
        return createAddressRequest;
    }

    public CreateAddressEvent createAddressRequest(CreateAddressRequest createAddressRequest) {
        assert !signed();
        this.createAddressRequest = createAddressRequest;
        return this;
    }
}
