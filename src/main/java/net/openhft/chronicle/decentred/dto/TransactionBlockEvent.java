package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.util.DtoParser;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.wire.IntConversion;
import net.openhft.chronicle.wire.UnsignedIntConverter;

public class TransactionBlockEvent<T> extends VanillaSignedMessage<TransactionBlockEvent<T>> {
    private transient DtoParser<T> dtoParser;

    private transient long messagesStart;

    @IntConversion(UnsignedIntConverter.class)
    private short chainId; // up to 64K chains

    @IntConversion(UnsignedIntConverter.class)
    private short weekNumber; // up to 1256 years

    @IntConversion(UnsignedIntConverter.class)
    private int blockNumber; // up to 7k/s on average

    public TransactionBlockEvent dtoParser(DtoParser<T> dtoParser) {
        this.dtoParser = dtoParser;
        return this;
    }

    @Override
    public void readMarshallable(BytesIn bytes) throws IORuntimeException {
        super.readMarshallable(bytes);
        messagesStart = bytes.readPosition();
    }

    public void replay(DtoRegistry<T> dtoRegistry, T allMessages) {
        if (dtoParser == null)
            dtoParser = dtoRegistry.get();
        long p0 = bytes.readPosition();
        bytes.readPosition(messagesStart);
        long limit = bytes.readLimit();
        try {
            while (!bytes.isEmpty()) {
                long position = bytes.readPosition();
                long length = bytes.readUnsignedInt(position);
                bytes.readLimit(position + length);
                dtoParser.parseOne(bytes, allMessages);
                bytes.readLimit(limit);
            }
        } finally {
            bytes.readLimit(limit);
            bytes.readPosition(p0);
        }
    }

    /*
    @IntConversion(RegionIntConverter.class)
    private int region;
    private int weekNumber;
    private long blockNumber; // unsigned int
    static public int MAX_16_BIT_NUMBER = 65536 - 1000;
    static int numberOfObjects = 0;
    private transient Bytes transactions;
    private transient int count;
    private transient DtoParser dtoParser;


    static public long _1_MB = 1 << 20;
    static public long _2_MB = 2 << 20;
    static public long _4_MB = 4 << 20;
    static public long _16_MB = 16 << 20;
    static public long _32_MB = 32 << 20;


    @Override
    public void reset() {
        super.reset();
        transactions.clear();
        count = 0;
    }

    public TransactionBlockEvent addTransaction(SignedBinaryMessage message) {
        count++;
        transactions.writeMarshallableLength16(message);
        //System.out.println("transactions writePosition " + transactions.writePosition() );
        return this;
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        reset();
        super.readMarshallable(wire);
        wire.read("transactions").sequence(this, (tbe, in) -> {
            while (in.hasNextSequenceItem()) {
                tbe.addTransaction(in.object(SignedBinaryMessage.class));
            }
        });
        //        System.out.println("Read " + this);
    }


    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        super.writeMarshallable(wire);
        wire.write("transactions").sequence(out -> replay(new WritingAllMessages() {
            @Override
            public WritingAllMessages to(long addressOrRegion) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void write(SignedBinaryMessage message) {
                out.object(message);
            }

            @Override
            public void close() {
                throw new UnsupportedOperationException();
            }
        }));
    }

}
*/
}