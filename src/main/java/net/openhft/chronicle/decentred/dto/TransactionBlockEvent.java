package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoParser;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.wire.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class TransactionBlockEvent<T> extends VanillaSignedMessage<TransactionBlockEvent<T>> {
    private transient DtoParser<T> dtoParser;

    // for writing to a new set of bytes
    private transient Bytes writeTransactions = Bytes.allocateElasticDirect(4L << 10);

    // where to read transactions from
    private transient long messagesStart;
    private transient Bytes transactions;
    private transient List<SignedMessage> transactionsList;

    @LongConversion(AddressLongConverter.class)
    private long chainAddress;

    @IntConversion(UnsignedIntConverter.class)
    private short weekNumber; // up to 1256 years

    @IntConversion(UnsignedIntConverter.class)
    private int blockNumber; // up to 7k/s on average

    public TransactionBlockEvent() {
        transactions = writeTransactions.clear();
        messagesStart = 0;
    }

    public TransactionBlockEvent dtoParser(DtoParser<T> dtoParser) {
        this.dtoParser = requireNonNull(dtoParser);
        return this;
    }

    @Override
    public void readMarshallable(BytesIn bytes) throws IORuntimeException {
        super.readMarshallable(bytes);
        messagesStart = this.bytes.readPosition();
        transactions = this.bytes;
    }

    public void replay(DtoRegistry<T> dtoRegistry, T allMessages) {
        if (dtoParser == null)
            dtoParser = dtoRegistry.get();
        replay(allMessages);
    }

    public void replay(T allMessages) {
        if (transactionsList != null) {
            for (SignedMessage signedMessage : transactionsList) {
                dtoParser.onMessage(allMessages, signedMessage);
            }
        }
        long p0 = transactions.readPosition();
        transactions.readPosition(messagesStart);
        long limit = transactions.readLimit();
        try {
            while (!transactions.isEmpty()) {
                long position = transactions.readPosition();
                long length = transactions.readUnsignedInt(position);
                transactions.readLimit(position + length);
                try {
                    dtoParser.parseOne(transactions, allMessages);
                } catch (Exception e) {
                    Jvm.warn().on(getClass(), "Error processing transaction event ", e);
                }
                transactions.readLimit(limit);
                transactions.readSkip(length);
            }
        } finally {
            transactions.readLimit(limit);
            transactions.readPosition(p0);
        }
    }

    @Override
    public void reset() {
        super.reset();
        transactions = writeTransactions.clear();
        messagesStart = 0;
    }

    public TransactionBlockEvent addTransaction(SignedMessage message) {
        if (!message.signed())
            throw new IllegalArgumentException(message + " must be already signed");
        message.writeMarshallable(writeTransactions);
        return this;
    }

    public boolean isEmpty() {
        return transactions.readRemaining() == 0 ||
                (transactionsList != null && !transactionsList.isEmpty());
    }

    @Override
    public void writeMarshallable0(BytesOut bytes) {
        super.writeMarshallable0(bytes);
        bytes.write(writeTransactions);
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) throws IORuntimeException {
        reset();
        super.readMarshallable(wire);
        if (transactionsList == null)
            transactionsList = new ArrayList<>();
        wire.read("transactions").sequence(this, (tbe, in) -> {
            while (in.hasNextSequenceItem()) {
                tbe.transactionsList.add(in.object(VanillaSignedMessage.class));
            }
        });
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        super.writeMarshallable(wire);
        if (dtoParser == null && transactionsList != null) {
            wire.write("transactions").sequence(transactionsList);

        } else {
            Class<T> superInterface = dtoParser.superInterface();
            //noinspection unchecked
            wire.write("transactions").sequence(out -> replay(
                    (T) Proxy.newProxyInstance(superInterface.getClassLoader(),
                            new Class[]{superInterface},
                            new AbstractMethodWriterInvocationHandler() {
                                @Override
                                protected void handleInvoke(Method method, Object[] args) {
                                    out.object(args[0]);
                                }
                            })));
        }
    }

    @NotNull
    @Override
    public <T> T deepCopy() {
        TransactionBlockEvent tbe = new TransactionBlockEvent();
        tbe.dtoParser = dtoParser;
        tbe.transactions = (transactions.readRemaining() == 0
                ? Bytes.elasticHeapByteBuffer(1)
                : transactions.copy().bytesForRead());
        tbe.transactionsList = transactionsList;
        return (T) tbe;
    }

    public long chainAddress() {
        return chainAddress;
    }

    public TransactionBlockEvent chainAddress(long chainAddress) {
        this.chainAddress = chainAddress;
        return this;
    }

    public int weekNumber() {
        return weekNumber & DecentredUtil.MASK_16;
    }

    public long blockNumber() {
        return blockNumber & DecentredUtil.MASK_32;
    }

    public TransactionBlockEvent blockNumber(long blockNumber) {
        assert !signed();
        this.blockNumber = (int) blockNumber;
        return this;
    }
}