package net.openhft.chronicle.decentred.dto.blockevent;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.base.TransientFieldHandler;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.dto.base.trait.HasChainAddress;
import net.openhft.chronicle.decentred.dto.base.trait.HasDtoParser;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.decentred.util.DtoParser;
import net.openhft.chronicle.wire.AbstractBytesMarshallable;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * An TransactionBlockEvent is a <em>chain event</em> that holds the transactions that is in a block.
 *
 */
public final class TransactionBlockEvent<T> extends VanillaSignedMessage<TransactionBlockEvent<T>> implements
        HasChainAddress<TransactionBlockEvent<T>>,
        HasDtoParser<TransactionBlockEvent<T>, T> {

    private static long INITIAL_TRANSACTION_CAPACITY_BYTES = 4L << 10;

    @LongConversion(AddressLongConverter.class)
    private long chainAddress;

    private transient DtoParser<T> dtoParser;
    private transient final Bytes transactions;

    public TransactionBlockEvent() {
        transactions = Bytes.allocateElasticDirect(INITIAL_TRANSACTION_CAPACITY_BYTES);
    }

    @Override
    public DtoParser<T> dtoParser() {
        return dtoParser;
    }

    @Override
    public TransactionBlockEvent<T> dtoParser(@NotNull DtoParser<T> dtoParser) {
        this.dtoParser = dtoParser;
        return this;
    }

    /**
     * Replays all transactions on the provided message handler.
     * <p>
     * Note: Messages used when invoking the provided message handler may be
     * reused. If messages are saved by the message handler, internal
     * copies must be made to ensure proper functionality.
     *
     * @param messageHandler message handler to replay messages on
     *
     * @throws IllegalStateException if this message has
     * not been signed before this method is called.
     */
    public void replay(@NotNull T messageHandler) {
        replay(b -> dtoParser.parseOne(b, messageHandler));
    }

    /**
     * Replays all transactions on the provided consumer.
     *
     * @param consumer to invoke on each message
     *
     * @throws IllegalStateException if this message has
     * not been signed before this method is called.
     */
    private void replay(@NotNull Consumer<Bytes> consumer) {
        assertSigned();
        transactions.readPosition(0);
        final long originalLimit = transactions.readLimit();
        try {
            while (!transactions.isEmpty()) {
                final long position = transactions.readPosition();
                final long length = transactions.readUnsignedInt(position);
                transactions.readLimit(position + length);
                try {
                    consumer.accept(transactions);  // The consumer may or may not affect the read position
                } catch (Exception e) {
                    Jvm.warn().on(getClass(), "Error processing transaction event ", e);
                }
             //   System.out.format("txid: %d, originalLimit: %d, length: %d, position: %d, readPosition:%d%n", System.identityHashCode(transactions), originalLimit, length, position, transactions.readPosition());

                transactions.readLimit(originalLimit);
                transactions.readPosition(position + length);
            }
        } finally {
            transactions.readLimit(originalLimit);
        }
    }

    /**
     * Adds a transaction to this message.
     *
     * @param message to add
     * @return this instance
     */
    public TransactionBlockEvent addTransaction(@NotNull SignedMessage message) {
        if (!message.signed()) {
            throw new IllegalArgumentException(
                String.format("The message of type %s, protocol %d, messageType %d has not been signed.", message.getClass().getSimpleName(), message.protocol(),  message.messageType())
            );
        }
        message.writeMarshallable(transactions);
        return this;
    }

    /**
     * Returns if this message's transaction queue is empty (of un-replayed transactions).
     * <p>
     *  If all transactions are replayed, the method returns {@code true},
     *  otherwise it returns {@code false}
     *
     * @return if this message's transaction queue is empty (of un-replayed transactions)
     */
    public boolean isEmpty() {
        return transactions.readRemaining() == 0;
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
            original.transactions.clear();
        }

        @Override
        public void copyNonMarshalled(@NotNull TransactionBlockEvent<T> original, @NotNull TransactionBlockEvent<T> target) {
            target.dtoParser(original.dtoParser());
        }

        @Override
        public void writeMarshallable(@NotNull TransactionBlockEvent<T> original, @NotNull WireOut wire) {
                wire.write("transactions").sequence(out ->
                    original.replay(b -> out.object(original.dtoParser.parseOne(b)))
                );
        }

        @Override
        public void readMarshallable(@NotNull TransactionBlockEvent<T> original, @NotNull WireIn wire) {
            wire.read("transactions").sequence(original, (tbe, in) -> {
                while (in.hasNextSequenceItem()) {
                    final WireIn wireIn = in.wireIn();
                    final SignedMessage m = in.typedMarshallable();
                    if (m instanceof AbstractBytesMarshallable) {
                        ((AbstractBytesMarshallable) m).readMarshallable(wireIn);
                    }
                    original.addTransaction(m);
                }
            });
        }

        @Override
        public void writeMarshallableInternal(@NotNull TransactionBlockEvent<T> original, @NotNull BytesOut bytes) {
            bytes.write(original.transactions);
        }

        @Override
        public void readMarshallable(@NotNull TransactionBlockEvent<T> original, @NotNull BytesIn bytes) {
            original.transactions.write(bytes);
        }
    }

}