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
import net.openhft.chronicle.wire.*;
import org.jetbrains.annotations.NotNull;

// Block number is N:th round in a week for the round.
/**
 * An TransactionBlockGossipEvent is a <em>chain event</em> that ...
 *
 */
public class TransactionBlockGossipEvent extends VanillaSignedMessage<TransactionBlockGossipEvent> implements
    HasChainAddress<TransactionBlockGossipEvent>,
    HasAddressToBlockNumberMap<TransactionBlockGossipEvent>
{
    @LongConversion(AddressLongConverter.class)
    private long chainAddress;
    private transient LongLongMap addressToBlockNumberMap;

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        super.readMarshallable(wire);
        wire.read("addressToBlockNumberMap").marshallable(in -> {
            while (in.hasMore()) {
                String key = in.readEvent(String.class);
                long addr = DecentredUtil.parseAddress(key);
                long value = in.getValueIn().int64();
                System.out.println("<GE " + key + " = " + value);
                addressToBlockNumberMap().justPut(addr, value);
            }
        });
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        super.writeMarshallable(wire);
        if (addressToBlockNumberMap != null && addressToBlockNumberMap.size() > 0)
            wire.write("addressToBlockNumberMap").marshallable(out -> {
                addressToBlockNumberMap.forEach((k, v) -> {
                    String key = DecentredUtil.toAddressString(k);
                    System.out.println(">GE " + key + " = " + v);
                    out.write(key);
                    out.getValueOut().int64(v);
                });
            });
    }

    @Override
    public void readMarshallable(BytesIn incomingBytes) throws IORuntimeException {
        super.readMarshallable(incomingBytes);
        chainAddress = bytes.readLong();

        int entries = (int) bytes.readStopBit();
        System.out.println("read gossip table entries = " + entries + " chain address " + DecentredUtil.toAddressString(chainAddress));
        if (addressToBlockNumberMap == null)
            addressToBlockNumberMap = LongLongMap.withExpectedSize(entries);
        for (int i = 0; i < entries; i++)
            addressToBlockNumberMap.justPut(bytes.readLong(), bytes.readUnsignedInt());
        assert !addressToBlockNumberMap.containsKey(0L);
    }

    @Override
    public void writeMarshallable0(BytesOut bytes) {  // was writeMarshallable
        super.writeMarshallable0(bytes);
        bytes.writeLong(chainAddress);
        int size = addressToBlockNumberMap.size();
        System.out.println("write gossip table entries = " + size + " chain address " + DecentredUtil.toAddressString(chainAddress));
        bytes.writeStopBit(size);
        addressToBlockNumberMap.forEach(new LongU32Writer(bytes));
    }

    @Override
    public long chainAddress() {
        return chainAddress;
    }

    @Override
    public TransactionBlockGossipEvent chainAddress(long chainAddress) {
        assert !signed();
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
    public void reset() {
            addressToBlockNumberMap.clear();
        if (addressToBlockNumberMap != null)
        super.reset();
    }
}
