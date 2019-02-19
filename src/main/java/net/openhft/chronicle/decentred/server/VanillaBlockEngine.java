package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.dto.*;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.threads.NamedThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;

public class VanillaBlockEngine<T> implements BlockEngine, Closeable {
    public static final int STEP_PAUSE_MILLIS = 5000;

    private final long address;
    private final long chainAddress;
    private final int periodUS;

    private final QueuingChainer chainer;
    private final VanillaGossiper gossiper;
    private final VanillaVoter voter;
    private final VanillaVoteTaker voteTaker;
    private final BlockReplayer blockReplayer;

    private final ExecutorService votingSes;
    private final ExecutorService processingSes;
    //    private final ExecutorService writerSes;
    private final long[] clusterAddresses;

    private long blockNumber = 0;
    private long nextSendUS;
    private MessageToListener tcpMessageListener;

    public <U extends T> VanillaBlockEngine(DtoRegistry<U> dtoRegistry,
                              long address,
                              long chainAddress,
                              int periodMS,
                              T postBlockChainProcessor,
                              long[] clusterAddresses) {
        this.address = address;
        this.chainAddress = chainAddress;
        this.periodUS = periodMS * 1000;

        nextSendUS = (SystemTimeProvider.INSTANCE.currentTimeMicros() / periodUS + 1) * periodUS;
        this.clusterAddresses = clusterAddresses;
        assert LongStream.of(clusterAddresses).anyMatch(a -> a == address);
        chainer = new QueuingChainer(chainAddress, dtoRegistry);
        blockReplayer = new VanillaBlockReplayer<>(address, dtoRegistry, postBlockChainProcessor);
        voteTaker = new VanillaVoteTaker(address, chainAddress, clusterAddresses, blockReplayer);
        voter = new VanillaVoter(address, clusterAddresses, voteTaker);
        gossiper = new VanillaGossiper(address, chainAddress, clusterAddresses, voter);
        String regionStr = DecentredUtil.toAddressString(chainAddress);
        votingSes = Executors.newSingleThreadExecutor(new NamedThreadFactory(regionStr + "-voter", true, Thread.MAX_PRIORITY));
        processingSes = Executors.newSingleThreadExecutor(new NamedThreadFactory(regionStr + "-processor", true, Thread.MAX_PRIORITY));
//        writerSes = Executors.newCachedThreadPool(new NamedThreadFactory(regionStr + "-writer", true, Thread.MIN_PRIORITY));
    }

    public static <T, U extends T> VanillaBlockEngine<T> newMain(DtoRegistry<U> dtoRegistry,
                                                    long address,
                                                    int periodMS,
                                                    long[] clusterAddresses,
                                                    T postBlockChainProcessor) {
        assert LongStream.of(clusterAddresses).distinct().count() == clusterAddresses.length;

        long main = DecentredUtil.parseAddress("main");

        return new VanillaBlockEngine<>(dtoRegistry, address, main, periodMS, postBlockChainProcessor, clusterAddresses);
    }

    public static <T, U extends T> VanillaBlockEngine<T> newLocal(DtoRegistry<U> dtoRegistry,
                                                     long address,
                                                     long chainAddress,
                                                     int periodMS,
                                                     long[] clusterAddresses,
                                                     T postBlockChainProcessor) {
        assert LongStream.of(clusterAddresses).distinct().count() == clusterAddresses.length;

        return new VanillaBlockEngine<>(dtoRegistry, address, chainAddress, periodMS, postBlockChainProcessor, clusterAddresses);
    }

    public void start(MessageToListener tcpMessageListener) {
        tcpMessageListener(tcpMessageListener);
        votingSes.submit(this::runVoter);

/*        for (Runnable runnable : tcpMessageListener.runnables()) {
            writerSes.submit(runnable);
        }*/
    }

    public void tcpMessageListener(MessageToListener tcpMessageListener) {
        this.tcpMessageListener = tcpMessageListener;
        voter.tcpMessageListener(tcpMessageListener);
        voteTaker.tcpMessageListener(tcpMessageListener);
        gossiper.tcpMessageToListener(tcpMessageListener);
    }

    public void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent) {
        blockReplayer.transactionBlockEvent(transactionBlockEvent);
        gossiper.transactionBlockEvent(transactionBlockEvent);
    }

    public void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent) {
        voter.transactionBlockGossipEvent(transactionBlockGossipEvent);
    }

    public void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent) {
        voteTaker.transactionBlockVoteEvent(transactionBlockVoteEvent);
    }

    public void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent) {
        blockReplayer.endOfRoundBlockEvent(endOfRoundBlockEvent);
    }

    @Override
    public void createChainRequest(CreateChainRequest createChainRequest) {
        chainer.onMessage(createChainRequest);
    }

    public void createTokenRequest(CreateTokenRequest createTokenRequest) {
        chainer.onMessage(createTokenRequest);
    }

    @Override
    public void createAddressRequest(CreateAddressRequest createAddressRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void verificationEvent(VerificationEvent verificationEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidationEvent(InvalidationEvent invalidationEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processOneBlock() {
        try {
            doProcessOneBlock();
            blockReplayer.replayBlocks();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    void runVoter() {
        try {
//            Histogram sample = new Histogram();
//            Histogram two = new Histogram();
//            Histogram three = new Histogram();
//            Histogram four = new Histogram();
//            int count = 0;
            while (!Thread.currentThread().isInterrupted()) {
//                long start = System.nanoTime();
                doProcessOneBlock();

                processingSes.submit(blockReplayer::replayBlocks);
                nextSendUS += periodUS;
                long delay = nextSendUS - SystemTimeProvider.INSTANCE.currentTimeMicros();
//                four.sample(System.nanoTime() - start);

                if (delay > 999)
                    Thread.sleep(delay / 1000);
//                if (delay > 10) // minimum delay
//                    LockSupport.parkNanos(delay * 1000);

/*
                if (++count % 100 == 0) {
                    System.out.println(sample.toMicrosFormat());
                    System.out.println(two.toMicrosFormat());
                    System.out.println(three.toMicrosFormat());
                    System.out.println(four.toMicrosFormat());
                }
*/

            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void doProcessOneBlock() throws InterruptedException {
        TransactionBlockEvent tbe = chainer.nextTransactionBlockEvent();
//                sample.sample(System.nanoTime() - start);
//                start = System.nanoTime();
        // tg System.out.println("TBE "+tbe);
        if (tbe != null) {
            tbe.address(address);
            tbe.blockNumber(blockNumber);
            for (long clusterAddress : clusterAddresses) {
                if (clusterAddress == address) {
                    transactionBlockEvent(tbe);
                } else {
                    tcpMessageListener.onMessageTo(clusterAddress, tbe);
                }
            }
            blockNumber++;
        }

//                int subRound = Math.max(100_000, periodUS * 100);
//                two.sample(System.nanoTime() - start);
        Thread.sleep(STEP_PAUSE_MILLIS);
//                LockSupport.parkNanos(subRound);
        gossiper.sendGossip(blockNumber);
        Thread.sleep(STEP_PAUSE_MILLIS);
//                LockSupport.parkNanos(subRound);

//                start = System.nanoTime();
        voter.sendVote(blockNumber);
//                three.sample(System.nanoTime() - start);

        Thread.sleep(STEP_PAUSE_MILLIS);
//                LockSupport.parkNanos(subRound);
//                start = System.nanoTime();
        //System.out.println(address + " " + blockNumber);
        if (voteTaker.hasMajority()) {
            if (voteTaker.sendEndOfRoundBlock(blockNumber))
                blockNumber++;
        }
    }

    @Override
    public void close() {
        votingSes.shutdownNow();
    }

    @Override
    public void onMessage(SignedMessage message) {
        chainer.onMessage(message);
    }
}
