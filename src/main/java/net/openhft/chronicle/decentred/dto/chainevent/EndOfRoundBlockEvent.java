package net.openhft.chronicle.decentred.dto.chainevent;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.dto.base.trait.HasAddressToBlockNumberMap;
import net.openhft.chronicle.decentred.dto.base.trait.HasBlockNumber;
import net.openhft.chronicle.decentred.dto.base.trait.HasChainAddress;
import net.openhft.chronicle.decentred.dto.base.trait.HasWeekNumber;
import net.openhft.chronicle.decentred.util.*;
import net.openhft.chronicle.wire.*;
import org.jetbrains.annotations.NotNull;

import static net.openhft.chronicle.decentred.util.DecentredUtil.MASK_16;
import static net.openhft.chronicle.decentred.util.DecentredUtil.MASK_32;

/**
 * An EndOfRoundBlockEvent is a <em>chain event</em> that notifies which block numbers are in the next round.
 *
 * Pointers for each address are included in this message with an association between an address and
 * the block number that particular address is currently at. Basically, these block numbers are like
 * a cursor which points to transactions.
 *
 * The cursors are monotonic pointers, usually in the order 0, 1, 2, ...
 */
// Add validation
public class EndOfRoundBlockEvent extends VanillaSignedMessage<EndOfRoundBlockEvent> implements
    HasWeekNumber<EndOfRoundBlockEvent>,
    HasChainAddress<EndOfRoundBlockEvent>,
    HasBlockNumber<EndOfRoundBlockEvent>,
    HasAddressToBlockNumberMap<EndOfRoundBlockEvent>
{
    @LongConversion(AddressLongConverter.class)
    private long chainAddress;
    @IntConversion(UnsignedIntConverter.class)
    private short weekNumber;
    @IntConversion(UnsignedIntConverter.class)
    private int blockNumber;
    private transient LongLongMap addressToBlockNumberMap;

    @Override
    public long chainAddress () {
        return chainAddress;
    }

    @Override
    public EndOfRoundBlockEvent chainAddress(long chainAddress) {
        this.chainAddress = chainAddress;
        return this;
    }

    @Override
    public int weekNumber() {
        return weekNumber & MASK_16;
    }

    @Override
    public EndOfRoundBlockEvent weekNumber(int weekNumber) {
        assertNotSigned();
        this.weekNumber = ShortUtil.toShortExact(weekNumber);
        return this;
    }

    @Override
    public long blockNumber() {
        return blockNumber & MASK_32;
    }

    @Override
    public EndOfRoundBlockEvent blockNumber(long blockNumber) {
        assertNotSigned();
        this.blockNumber = Math.toIntExact(blockNumber);
        return this;
    }

    @Override
    public LongLongMap addressToBlockNumberMap() {
        if (addressToBlockNumberMap == null)
            addressToBlockNumberMap = LongLongMap.withExpectedSize(16);
        return addressToBlockNumberMap;
    }

    @Override
    public void readMarshallable(BytesIn bytes) throws IORuntimeException {
        super.readMarshallable(bytes);
        final int entries = (int) this.bytes.readStopBit();
        addressToBlockNumberMap = LongLongMap.withExpectedSize(entries);
        for (int i = 0; i < entries; i++) {
            addressToBlockNumberMap.justPut(this.bytes.readLong(), this.bytes.readUnsignedInt());
        }
    }

    @Override
    protected void writeMarshallable0(BytesOut bytes) {
        super.writeMarshallable0(bytes);
        bytes.writeStopBit(addressToBlockNumberMap.size());
        addressToBlockNumberMap.forEach(new LongU32Writer(bytes));
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        super.readMarshallable(wire);
        wire.read("addressToBlockNumberMap").marshallable(in -> {
            while (in.hasMore()) {
                String key = in.readEvent(String.class);
                long addr = DecentredUtil.parseAddress(key);
                long value = in.getValueIn().int64();
                addressToBlockNumberMap().justPut(addr, value);
            }
        });
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        super.writeMarshallable(wire);
        wire.write("addressToBlockNumberMap").marshallable(out -> {
            addressToBlockNumberMap.forEach((k, v) -> out.write(DecentredUtil.toAddressString(k)).int64(v));
        });
    }

    @NotNull
    @Override
    public <T> T deepCopy() {
        EndOfRoundBlockEvent eorbe = new EndOfRoundBlockEvent();
        eorbe.addressToBlockNumberMap = LongLongMap.withExpectedSize(addressToBlockNumberMap.size());
        eorbe.addressToBlockNumberMap.putAll(addressToBlockNumberMap);
        return (T) eorbe;
    }
}

