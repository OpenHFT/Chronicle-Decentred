package town.lost.examples.appreciation.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.wire.LongConversion;

public class Give extends VanillaSignedMessage<Give> {
    @LongConversion(AddressLongConverter.class)
    private long toAddress;

    private double amount;

    public Give() {
    }

    public Give(long toAddress, double amount) {
        init(toAddress, amount);
    }

    public Give init(long toAddress, double amount) {
        this.toAddress = toAddress;
        this.amount = amount;
        return this;
    }

    public long toAddress() {
        return toAddress;
    }

    public Give toAddress(long toAddress) {
        this.toAddress = toAddress;
        return this;
    }

    public double amount() {
        return amount;
    }

    public Give amount(double amount) {
        this.amount = amount;
        return this;
    }

    @Override
    protected void readMarshallableInternal(Bytes bytes) {
        timestampUS = bytes.readLong();
        address = bytes.readLong();
        toAddress = bytes.readLong();
        amount = bytes.readDouble();
    }

    @Override
    protected void writeMarshallableInternal(BytesOut bytes) {
        bytes.writeLong(timestampUS);
        bytes.writeLong(address);
        bytes.writeLong(toAddress);
        bytes.writeDouble(amount);
    }
}
