package net.openhft.chronicle.decentred.internal.server;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.io.AbstractCloseable;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.address.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.base.SignedMessage;
import net.openhft.chronicle.decentred.dto.blockevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.dto.chainlifecycle.AssignDelegatesRequest;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateChainRequest;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateTokenRequest;
import net.openhft.chronicle.decentred.server.*;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.threads.NamedThreadFactory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;

public class VanillaBlockEngine<T> extends AbstractCloseable implements BlockEngine, Closeable {
    public static final int STEP_PAUSE_MILLIS = 25;

    private final long address;
    private final long chainAddress;
    private final int periodUS;

    private final Chainer chainer;
    private final VanillaGossiper gossiper;
    private final Voter voter;
    private final VoteTaker voteTaker;
    private final BlockReplayer blockReplayer;

    private final ExecutorService votingSes;
    private final ExecutorService processingSes;
    //    private final ExecutorService writerSes;
    private final long[] clusterAddresses;
    private final BytesStore secretKey;

    private long blockNumber = 0;
    private long nextSendUS;
    private MessageToListener tcpMessageListener;

    static {
        System.out.println("Step pause millis: " + STEP_PAUSE_MILLIS);
    }

    public <U extends T> VanillaBlockEngine(@NotNull DtoRegistry<U> dtoRegistry,
                                            KeyPair keyPair,
                                            long chainAddress,
                                            int periodMS,
                                            @NotNull T postBlockChainProcessor,
                                            @NotNull long[] clusterAddresses,
                                            @NotNull TimeProvider timeProvider) {
        this.address = keyPair.address();
        this.chainAddress = chainAddress;
        this.periodUS = periodMS * 1000;
        this.secretKey = keyPair.secretKey;

        nextSendUS = (SystemTimeProvider.INSTANCE.currentTimeMicros() / periodUS + 1) * periodUS;
        this.clusterAddresses = clusterAddresses;
        assert LongStream.of(clusterAddresses).anyMatch(a -> a == address);
        chainer = Chainer.createQueuing(chainAddress, dtoRegistry);
        blockReplayer = new VanillaBlockReplayer<>(address, dtoRegistry, postBlockChainProcessor);
        voteTaker = VoteTaker.create(address, chainAddress, clusterAddresses, blockReplayer, keyPair.secretKey, dtoRegistry);
        voter = Voter.createLastGossipVoter(address, clusterAddresses, voteTaker, keyPair.secretKey, dtoRegistry);
        gossiper = new VanillaGossiper(keyPair, dtoRegistry, chainAddress, clusterAddresses, voter, timeProvider);
        String regionStr = DecentredUtil.toAddressString(chainAddress);
        votingSes = Executors.newSingleThreadExecutor(new NamedThreadFactory(regionStr + "-voter", true, Thread.MAX_PRIORITY));
        processingSes = Executors.newSingleThreadExecutor(new NamedThreadFactory(regionStr + "-processor", true, Thread.MAX_PRIORITY));
//        writerSes = Executors.newCachedThreadPool(new NamedThreadFactory(regionStr + "-writer", true, Thread.MIN_PRIORITY));
    }

    @Override
    public void start(@NotNull MessageToListener tcpMessageListener) {
        tcpMessageListener(tcpMessageListener);
        votingSes.submit(this::runVoter);

/*        for (Runnable runnable : tcpMessageListener.runnables()) {
            writerSes.submit(runnable);
        }*/
    }

    @Override
    public void tcpMessageListener(@NotNull MessageToListener tcpMessageListener) {
        this.tcpMessageListener = tcpMessageListener;
        voter.tcpMessageListener(tcpMessageListener);
        voteTaker.tcpMessageListener(tcpMessageListener);
        gossiper.tcpMessageToListener(tcpMessageListener);
    }

    @Override
    public void transactionBlockEvent(@NotNull TransactionBlockEvent transactionBlockEvent) {
        blockReplayer.transactionBlockEvent(transactionBlockEvent);
        gossiper.transactionBlockEvent(transactionBlockEvent);
    }

    @Override
    public void transactionBlockGossipEvent(@NotNull TransactionBlockGossipEvent transactionBlockGossipEvent) {
        voter.transactionBlockGossipEvent(transactionBlockGossipEvent);
    }

    @Override
    public void transactionBlockVoteEvent(@NotNull TransactionBlockVoteEvent transactionBlockVoteEvent) {
        voteTaker.transactionBlockVoteEvent(transactionBlockVoteEvent);
    }

    @Override
    public void endOfRoundBlockEvent(@NotNull EndOfRoundBlockEvent endOfRoundBlockEvent) {
        blockReplayer.endOfRoundBlockEvent(endOfRoundBlockEvent);
    }

    @Override
    public void createChainRequest(@NotNull CreateChainRequest createChainRequest) {
        chainer.onMessage(createChainRequest);
    }

    @Override
    public void assignDelegatesRequest(@NotNull AssignDelegatesRequest assignDelegatesRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createTokenRequest(@NotNull CreateTokenRequest createTokenRequest) {
        chainer.onMessage(createTokenRequest);
    }

    @Override
    public void createAddressRequest(@NotNull CreateAddressRequest createAddressRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void verificationEvent(@NotNull VerificationEvent verificationEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidationEvent(@NotNull InvalidationEvent invalidationEvent) {
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
        final TransactionBlockEvent tbe = chainer.nextTransactionBlockEvent();
//                sample.sample(System.nanoTime() - start);
//                start = System.nanoTime();
        // tg System.out.println("TBE "+tbe);
        if (tbe != null) {
            tbe.address(address);
            tbe.sign(secretKey);
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
    protected void performClose() {
        votingSes.shutdownNow();
    }

    @Override
    public void onMessage(SignedMessage message) {
        chainer.onMessage(message);
    }
}
