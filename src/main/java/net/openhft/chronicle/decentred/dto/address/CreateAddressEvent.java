package net.openhft.chronicle.decentred.dto.address;

// Confirms that the CreateAddressRequest was approved

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;

// The intention is to add a message that can associate an address to a server list (IP-addresses) authorities()
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
