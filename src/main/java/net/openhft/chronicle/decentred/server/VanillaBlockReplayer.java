package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.blockevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.util.DtoParser;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.LongLongMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// We assume that when we have received message with timestamp X,
// we have also received all messages with timestamp X' such that X' < X
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

                    TransactionLog log = entry.getValue();
                    while (true) {
                        if (log.getNewestTimestamp() >= upto) {
                            break;
                        }
                        // System.out.println(address + " Waiting ... " + size + " < " + upto);
                        wait(100);
                    }

                    if (last < upto) {
                        replayActions.add(() -> replay(log, last + 1, upto + 1));
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
        replayActions.forEach(Runnable::run);
//        postBlockChainProcessor.replayFinished();
    }

    private void replay(TransactionLog log, long fromTs, long toTs) {
        for (SignedMessage message: log.get(fromTs, toTs)) {
            if (message instanceof TransactionBlockEvent) {
                TransactionBlockEvent tbe = (TransactionBlockEvent) message;
                tbe.dtoParser(dtoParser);
                tbe.replay(postBlockChainProcessor);
            }
        }
 }

    static class TransactionLog {
        // To be replaced by some other off-heap map
        private final SortedMap<Long, SignedMessage> messages = new TreeMap<>();

        public synchronized void add(SignedMessage msg) {
            if (msg instanceof TransactionBlockEvent) {
                messages.put(msg.timestampUS(), msg);
            } else if (msg instanceof EndOfRoundBlockEvent) {
                messages.put(msg.timestampUS(), msg);
            } else {
                Jvm.warn().on(getClass(), "Unknown " + msg.getClass());
            }
        }

        public synchronized long getNewestTimestamp() {
            return messages.lastKey();
        }

        public Iterable<? extends SignedMessage> get(long fromTs, long toTs) {
            synchronized (this) {
                return messages.tailMap(fromTs).headMap(toTs).values();
            }
        }
    }
}
