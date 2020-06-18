package net.openhft.chronicle.decentred.dto.chainlifecycle;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.OffsetIntConverter;
import net.openhft.chronicle.wire.IntConversion;

// Dynamic lifecycle of a chain
// One chain per address
//     private int cycleOffset; private int roundsPerDay; ignored as of now

//Create address -> CreateChain -> Delegate Chain

public final class CreateChainRequest extends VanillaSignedMessage<CreateChainRequest> {

    @IntConversion(OffsetIntConverter.class)
    private int cycleOffset;
    private int roundsPerDay;

    public int cycleOffset() {
        return cycleOffset;
    }

    public CreateChainRequest cycleOffset(int cycleOffset) {
        assertNotSigned();
        this.cycleOffset = cycleOffset;
        return this;
    }

    public int roundsPerDay() {
        return roundsPerDay;
    }

    public CreateChainRequest roundsPerDay(int roundsPerDay) {
        assertNotSigned();
        this.roundsPerDay = roundsPerDay;
        return this;
    }

}
