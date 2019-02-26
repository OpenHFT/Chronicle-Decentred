package net.openhft.chronicle.decentred.dto.fundamental.address;

import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.fundamental.AbstractFundamentalDtoTest;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class CreateAddressRequestFundamentalTest extends AbstractFundamentalDtoTest<CreateAddressRequest> {

    public CreateAddressRequestFundamentalTest() {
        super(CreateAddressRequest::new);
    }

    @Override
    protected void initializeSpecifics(CreateAddressRequest message) {}

    @Override
    protected void assertInitializedSpecifics(CreateAddressRequest message) {}

    @Override
    protected void assertInitializedToString(String s) {}

    @Override
    protected Stream<Map.Entry<String, Consumer<CreateAddressRequest>>> forbiddenAfterSign() {
        return Stream.empty();
    }
}
