package net.openhft.chronicle.decentred.dto;

import java.time.ZonedDateTime;

public class CreateChainRequest extends VanillaSignedMessage<CreateChainRequest> {
    private Cycle cycle;
    private ZonedDateTime epochTime;
    private int roundsPerWeek;
}
