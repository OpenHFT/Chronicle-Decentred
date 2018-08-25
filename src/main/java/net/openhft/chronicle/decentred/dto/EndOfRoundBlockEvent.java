package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.decentred.util.AddressConverter;
import net.openhft.chronicle.decentred.util.LongLongMap;
import net.openhft.chronicle.decentred.util.LongU32Writer;
import net.openhft.chronicle.wire.IntConversion;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.UnsignedIntConverter;

import static net.openhft.chronicle.decentred.util.DecentredUtil.MASK_16;
import static net.openhft.chronicle.decentred.util.DecentredUtil.MASK_32;

public class EndOfRoundBlockEvent extends VanillaSignedMessage<EndOfRoundBlockEvent> {
    @LongConversion(AddressConverter.class)
    private long chainAddress;
    @IntConversion(UnsignedIntConverter.class)
    private short weekNumber;
    @IntConversion(UnsignedIntConverter.class)
    private int blockNumber;
    private LongLongMap blockRecords;
    private transient LongU32Writer longU32Writer;

    public long chainAddress() {
        return chainAddress;
    }

    public EndOfRoundBlockEvent chainAddress(long chainAddress) {
        this.chainAddress = chainAddress;
        return this;
    }

    public int weekNumber() {
        return weekNumber & MASK_16;
    }

    public EndOfRoundBlockEvent weekNumber(int weekNumber) {
        this.weekNumber = (short) weekNumber;
        return this;
    }

    public long blockNumber() {
        return blockNumber & MASK_32;
    }

    public EndOfRoundBlockEvent blockNumber(long blockNumber) {
        this.blockNumber = (int) blockNumber;
        return this;
    }

    public LongLongMap blockRecords() {
        if (blockRecords == null)
            blockRecords = LongLongMap.withExpectedSize(16);
        return blockRecords;
    }
}

