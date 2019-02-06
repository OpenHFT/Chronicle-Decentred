package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.decentred.util.OffsetIntConverter;
import net.openhft.chronicle.wire.IntConversion;

public class CreateChainRequest extends VanillaSignedMessage<CreateChainRequest> {
    @IntConversion(OffsetIntConverter.class)
    private int cycleOffset;
    private int roundsPerDay;

    public int cycleOffset() {
        return cycleOffset;
    }

    public CreateChainRequest cycleOffset(int cycleOffset) {
        this.cycleOffset = cycleOffset;
        return this;
    }

    public int roundsPerDay() {
        return roundsPerDay;
    }

    public CreateChainRequest roundsPerDay(int roundsPerDay) {
        this.roundsPerDay = roundsPerDay;
        return this;
    }
}
