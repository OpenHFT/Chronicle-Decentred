package net.openhft.chronicle.decentred.dto.chainevent;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.dto.base.trait.HasAddressToBlockNumberMap;
import net.openhft.chronicle.decentred.dto.base.trait.HasChainAddress;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.LongLongMap;
import net.openhft.chronicle.decentred.util.LongU32Writer;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * An EndOfRoundBlockEvent is a <em>chain event</em> that notifies which block numbers are in the next round.
 * <p>
 * Pointers for each address are included in this message with an association between an address and
 * the block number that particular address is currently at. Basically, these block numbers are like
 * a cursor which points to transactions.
 * <p>
 * The cursors are monotonic pointers, usually in the order 0, 1, 2, ...
 */
// Add validation
public class EndOfRoundBlockEvent extends VanillaSignedMessage<EndOfRoundBlockEvent> implements
        HasChainAddress<EndOfRoundBlockEvent>,
        HasAddressToBlockNumberMap<EndOfRoundBlockEvent> {
    @LongConversion(AddressLongConverter.class)
    private long chainAddress;
    private transient LongLongMap addressToBlockNumberMap;

    static void writeMap(@NotNull WireOut wire, String name, LongLongMap map) {
        if (map == null || map.size() == 0)
            return;
        wire.write(name).marshallable(out -> {
            long[] keys = map.keySet().toLongArray();
            for (int i = 0; i < keys.length; i++) keys[i] -= Long.MIN_VALUE;
            Arrays.sort(keys);
            for (int i = 0; i < keys.length; i++) keys[i] += Long.MIN_VALUE;
            for (long key : keys) {
                out.write(DecentredUtil.toAddressString(key)).int64(map.get(key));
            }
        });
    }

    @Override
    public EndOfRoundBlockEvent chainAddress(long chainAddress) {
        assertNotSigned();
        this.chainAddress = chainAddress;
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
    public long chainAddress() {
        return chainAddress;
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        super.writeMarshallable(wire);
        writeMap(wire, "addressToBlockNumberMap", addressToBlockNumberMap);
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

