package net.openhft.chronicle.decentred.dto.chainevent;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.base.TransientFieldHandler;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.dto.base.trait.HasChainAddress;
import net.openhft.chronicle.decentred.util.*;
import net.openhft.chronicle.wire.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.openhft.chronicle.decentred.dto.chainevent.AddressToBlockNumberUtil.ADDRESS_TO_BLOCK_NUMBER_MAP_NAME;

/**
 * An TransactionBlockEvent is a <em>chain event</em> that holds the transactions that is in a block.
 *
 */
public final class TransactionBlockEvent<T> extends VanillaSignedMessage<TransactionBlockEvent<T>> implements
        HasChainAddress<TransactionBlockEvent<T>> {

    private transient DtoParser<T> dtoParser;

    // for writing to a new set of bytes
    private transient Bytes writeTransactions = Bytes.allocateElasticDirect(4L << 10);

    // where to read transactions from
    private transient long messagesStart;
    private transient Bytes transactions;
    private transient List<SignedMessage> transactionsList;  // TODO: CHECK if this can be removed.

    @LongConversion(AddressLongConverter.class)
    private long chainAddress;

    public TransactionBlockEvent() {
        transactions = writeTransactions.clear();
        messagesStart = 0;
    }

    public TransactionBlockEvent dtoParser(DtoParser<T> dtoParser) {
        this.dtoParser = requireNonNull(dtoParser);
        return this;
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
    public long chainAddress() {
        return chainAddress;
    }

    @Override
    public TransactionBlockEvent<T> chainAddress(long chainAddress) {
        assertNotSigned();
        this.chainAddress = chainAddress;
        return this;
    }


    // Handling of transient fields

    private final transient TransientFieldHandler<TransactionBlockEvent<T>> transientFieldHandler = new CustomTransientFieldHandler();

    @Override
    public TransientFieldHandler<TransactionBlockEvent<T>> transientFieldHandler() {
        return transientFieldHandler;
    }


    private final class CustomTransientFieldHandler implements TransientFieldHandler<TransactionBlockEvent<T>> {

        @Override
        public void reset(TransactionBlockEvent<T> original) {
            original.dtoParser = null;
            original.messagesStart = 0;
            original.writeTransactions = writeTransactions.clear();
            original.transactionsList = null;
        }

        @Override
        public void copy(@NotNull TransactionBlockEvent<T> original, @NotNull TransactionBlockEvent<T> target) {
            throw new UnsupportedOperationException("This method is unsafe and creates references to shared memory");
/*            target.dtoParser = original.dtoParser;
            target.messagesStart = original.messagesStart;
            target.writeTransactions = original.writeTransactions;
            target.transactionsList = new ArrayList<>(original.transactionsList);*/
        }

        @Override
        public void deepCopy(@NotNull TransactionBlockEvent<T> original, @NotNull TransactionBlockEvent<T> target) {
            target.dtoParser = original.dtoParser;
            target.transactions = (original.transactions.readRemaining() == 0
                ? Bytes.elasticHeapByteBuffer(1)
                : original.transactions.copy().bytesForRead());

            // Todo: Handle the other volatile parameters!!
        }

        @Override
        public void writeMarshallable(@NotNull TransactionBlockEvent<T> original, @NotNull WireOut wire) {
            // Todo: Handle the other volatile parameters!!
            if (original.dtoParser == null && original.transactionsList != null) {
                wire.write("transactions").sequence(original.transactionsList);
            } else {
                final Class<T> superInterface = original.dtoParser.superInterface();
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

        @Override
        public void readMarshallable(@NotNull TransactionBlockEvent<T> original, @NotNull WireIn wire) {
            // Todo: Handle the other volatile parameters!!
            if (original.transactionsList == null) {
                original.transactionsList = new ArrayList<>();
            }
            wire.read("transactions").sequence(original, (tbe, in) -> {
                while (in.hasNextSequenceItem()) {
                    tbe.transactionsList.add(in.object(VanillaSignedMessage.class));
                }
            });

        }

        @Override
        public void writeMarshallableInternal(@NotNull TransactionBlockEvent<T> original, @NotNull BytesOut bytes) {
            // Todo: Handle the other volatile parameters!!
            bytes.write(original.writeTransactions);
        }

        @Override
        public void readMarshallable(@NotNull TransactionBlockEvent<T> original, @NotNull BytesIn bytes) {
            // Todo: Handle the other volatile parameters!!
            messagesStart = original.bytes.readPosition();
            transactions = original.bytes;
        }
    }


}