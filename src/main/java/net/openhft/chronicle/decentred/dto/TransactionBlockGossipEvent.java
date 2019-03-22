package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.LongLongMap;
import net.openhft.chronicle.decentred.util.LongU32Writer;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

public class TransactionBlockGossipEvent extends VanillaSignedMessage<TransactionBlockGossipEvent> {
    @LongConversion(AddressLongConverter.class)
    private long chainAddress;
    private short weekNumber; // up to 1256 years
    private int blockNumber; // up to 7k/s on average
    private transient LongLongMap addressToBlockNumberMap;
    private transient LongU32Writer longU32Writer;

    public TransactionBlockGossipEvent blockNumber(long blockNumber) {
        this.blockNumber = (int) blockNumber;
        return this;
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        super.readMarshallable(wire);
        wire.read("addressToBlockNumberMap").marshallable(in -> {
            while (in.hasMore()) {
                String key = in.readEvent(String.class);
                if (key == null || key.length() == 0)
                    return;
                long addr = DecentredUtil.parseAddress(key);
                long value = in.getValueIn().int64();
                addressToBlockNumberMap().justPut(addr, value);
            }
        });
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        super.writeMarshallable(wire);
        if (addressToBlockNumberMap != null && addressToBlockNumberMap.size() > 0)
            wire.write("addressToBlockNumberMap").marshallable(out -> {
                addressToBlockNumberMap.forEach((k, v) -> out.write(DecentredUtil.toAddressString(k)).int64(v));
            });
    }

    @Override
    protected void readMarshallable0(BytesIn bytes) throws IORuntimeException {
        chainAddress = bytes.readLong();
        weekNumber = bytes.readShort();
        blockNumber = bytes.readInt();
        int entries = (int) bytes.readStopBit();
        if (addressToBlockNumberMap == null)
            addressToBlockNumberMap = LongLongMap.withExpectedSize(entries);
        for (int i = 0; i < entries; i++)
            addressToBlockNumberMap.justPut(bytes.readLong(), bytes.readUnsignedInt());
        assert !addressToBlockNumberMap.containsKey(0L);
    }

    @Override
    protected void writeMarshallable0(BytesOut bytes) {
        bytes.writeLong(chainAddress);
        bytes.writeShort(weekNumber);
        bytes.writeInt(blockNumber);
        bytes.writeStopBit(addressToBlockNumberMap.size());
        if (longU32Writer == null) {
            longU32Writer = new LongU32Writer();
        }
        longU32Writer.bytes(bytes);
        addressToBlockNumberMap.forEach(longU32Writer);
    }

    public long chainAddress() {
        return chainAddress;
    }

    public TransactionBlockGossipEvent chainAddress(long chainAddress) {
        this.chainAddress = chainAddress;
        return this;
    }

    public LongLongMap addressToBlockNumberMap() {
        if (addressToBlockNumberMap == null)
            addressToBlockNumberMap = LongLongMap.withExpectedSize(16);
        return addressToBlockNumberMap;
    }
}
