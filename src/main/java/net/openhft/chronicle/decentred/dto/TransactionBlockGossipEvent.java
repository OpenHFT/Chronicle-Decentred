package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.util.LongLongMap;
import net.openhft.chronicle.decentred.util.LongU32Writer;

public class TransactionBlockGossipEvent extends VanillaSignedMessage<TransactionBlockGossipEvent> {
    private transient LongU32Writer longU32Writer;
    private long chainAddress;
    private short weekNumber; // up to 1256 years
    private int blockNumber; // up to 7k/s on average
    private LongLongMap addressToBlockNumberMap;

    public TransactionBlockGossipEvent blockNumber(long blockNumber) {
        this.blockNumber = (int) blockNumber;
        return this;
    }

    @Override
    public void readMarshallable(BytesIn bytes) throws IORuntimeException {
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
    public void writeMarshallable(BytesOut bytes) {
        assert !addressToBlockNumberMap.containsKey(0L);
        bytes.writeLong(chainAddress);
        bytes.writeShort(weekNumber);
        bytes.writeInt(blockNumber);
        bytes.writeStopBit(addressToBlockNumberMap.size());
        if (longU32Writer == null) longU32Writer = new LongU32Writer();
        longU32Writer.bytes = bytes;
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
        return addressToBlockNumberMap;
    }
}
