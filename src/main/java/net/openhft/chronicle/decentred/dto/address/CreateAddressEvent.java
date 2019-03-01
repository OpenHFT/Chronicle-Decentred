package net.openhft.chronicle.decentred.dto.address;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;

// Confirms that the CreateAddressRequest was approved

// The intention is to add a message that can associate an address to a server list (IP-addresses) authorities()
public final class CreateAddressEvent extends VanillaSignedMessage<CreateAddressEvent> {

    private CreateAddressRequest createAddressRequest;

    public CreateAddressRequest createAddressRequest() {
        return createAddressRequest;
    }

    public CreateAddressEvent createAddressRequest(CreateAddressRequest createAddressRequest) {
        assertNotSigned();
        this.createAddressRequest = createAddressRequest;
        return this;
    }

}
