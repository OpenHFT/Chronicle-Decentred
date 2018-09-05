package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.wire.Base85LongConverter;
import net.openhft.chronicle.wire.LongConversion;

public class CreateTokenRequest extends VanillaSignedMessage<CreateTokenRequest> {
    @LongConversion(Base85LongConverter.class)
    private long symbol;
    private double amount;
    private double granularity;
}
