package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.dto.chainevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.chainevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.util.DtoParser;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.LongLongMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VanillaBlockReplayer<T> implements BlockReplayer {
    private final long address;
    private final T postBlockChainProcessor;

    private Map<Long, TransactionLog> transactionLogMap = new ConcurrentHashMap<>();
    private EndOfRoundBlockEvent lastEndOfRoundBlockEvent = null;
    private LongLongMap replayedMap = LongLongMap.withExpectedSize(16);
    private DtoParser dtoParser;

    public <U extends T> VanillaBlockReplayer(long address, DtoRegistry<U> dtoRegistry, T postBlockChainProcessor) {
        this.address = address;
        dtoParser = dtoRegistry.get();
        this.postBlockChainProcessor = postBlockChainProcessor;
    }

    @Override
    public synchronized void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent) {
        transactionLogMap.computeIfAbsent(transactionBlockEvent.address(), k -> new TransactionLog())
                .add(transactionBlockEvent);
        notifyAll();
    }

    @Override
    public synchronized void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent) {
        transactionLogMap.computeIfAbsent(endOfRoundBlockEvent.address(), k -> new TransactionLog())
                .add(endOfRoundBlockEvent);
        lastEndOfRoundBlockEvent = endOfRoundBlockEvent;
        notifyAll();
    }

    // TODO Ideally blocks should be replayed in order, though it doesn't matter much esp if they are all up to date.
    @Override
    public void replayBlocks() {
        List<Runnable> replayActions = new ArrayList<>();
        synchronized (this) {
            if (lastEndOfRoundBlockEvent == null) {
//                Jvm.warn().on(getClass(), "No EndOfRoundBlockEvent to process");
                return;
            }
            try {
                for (Map.Entry<Long, TransactionLog> entry : transactionLogMap.entrySet()) {
                    long upto = lastEndOfRoundBlockEvent.addressToBlockNumberMap()
                            .getOrDefault(entry.getKey(), -1L);
                    if (upto == -1L) {
                        continue;
                    }

                    long last = replayedMap.getOrDefault(entry.getKey(), -1L);

                    int size;
                    while (true) {
                        size = entry.getValue().messages.size();
                        if (size >= upto) {
                            break;
                        }
                        System.out.println(address + " Waiting ... " + size + " < " + upto);
                        wait(100);
                    }

                    if (last < size) {
                        replayActions.add(() -> replay(entry.getValue(), last + 1, upto));
                        replayedMap.justPut(entry.getKey(), upto);
                    }
                }
            } catch (InterruptedException ie) {
                Jvm.warn().on(getClass(), "Giving up waiting - interrupted");
                Thread.currentThread().interrupt();
            }
            lastEndOfRoundBlockEvent = null;
        }
//        postBlockChainProcessor.replayStarted();
        for (Runnable replayAction : replayActions) {
            replayAction.run();
        }
//        postBlockChainProcessor.replayFinished();
    }

    private void replay(TransactionLog messages, long fromIndex, long toIndex) {
        for (long i = fromIndex; i <= toIndex; i++) {
            SignedMessage message = messages.get((int) i);
            if (message instanceof TransactionBlockEvent) {
                TransactionBlockEvent tbe = (TransactionBlockEvent) message;
                tbe.dtoParser(dtoParser);
                tbe.replay(postBlockChainProcessor);
            }
        }

    }

    static class TransactionLog {
        private final List<SignedMessage> messages = new ArrayList<>();

        public void add(TransactionBlockEvent transactionBlockEvent) {
            add(transactionBlockEvent, (int) transactionBlockEvent.blockNumber());
        }

        public void add(EndOfRoundBlockEvent endOfRoundBlockEvent) {
            add(endOfRoundBlockEvent, (int) endOfRoundBlockEvent.blockNumber());
        }

        synchronized void add(SignedMessage msg, int blockNumber) {
            if (blockNumber < messages.size()) {
                System.out.println("Duplicate message id: " + blockNumber + " size: " + messages.size() + " was " + msg.getClass());
            } else if (blockNumber > messages.size()) {
                System.out.println("Missing message id: " + blockNumber);
            } else if (msg instanceof TransactionBlockEvent) {
                messages.add(((TransactionBlockEvent<?>) msg).deepCopy());
            } else if (msg instanceof EndOfRoundBlockEvent) {
                messages.add(((EndOfRoundBlockEvent) msg).deepCopy());
            } else {
                Jvm.warn().on(getClass(), "Unknown " + msg.getClass());
            }
        }

        synchronized SignedMessage get(int index) {
            return messages.get(index);
        }
    }
}
