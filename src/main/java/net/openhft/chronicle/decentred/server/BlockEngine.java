package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.api.WeeklyEvents;
import net.openhft.chronicle.decentred.dto.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.threads.NamedThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockEngine<T> implements MessageListener, WeeklyEvents, Closeable {
    private final MessageListener messageListener;
    private final long address;
    private final long chainAddress;
    private final int periodUS;

    private final T fastPath;
    private final Chainer chainer;
    private final Gossiper gossiper;
    private final Voter voter;
    private final VoteTaker voteTaker;
    private final BlockReplayer blockReplayer;
    private final T postBlockChainProcessor;

    private final TransactionBlockGossipEvent tbge;
    private final ExecutorService votingSes;
    private final ExecutorService processingSes;
    private final ExecutorService writerSes;
    private final long[] clusterAddresses;
    long blockNumber = 0;
    private long nextSendUS;
    private MessageWriter messageWriter;

    public BlockEngine(MessageListener messageListener,
                       DtoRegistry<T> dtoRegistry,
                       long address,
                       long chainAddress,
                       int periodMS,
                       T fastPath,
                       Chainer chainer,
                       T postBlockChainProcessor,
                       long[] clusterAddresses) {
        this.messageListener = messageListener;
        this.address = address;
        this.chainAddress = chainAddress;
        this.periodUS = periodMS * 1000;
        this.fastPath = fastPath;
        this.chainer = chainer;
        this.postBlockChainProcessor = postBlockChainProcessor;
        tbge = new TransactionBlockGossipEvent();
        nextSendUS = (SystemTimeProvider.INSTANCE.currentTimeMicros() / periodUS + 1) * periodUS;
        this.clusterAddresses = clusterAddresses;
        gossiper = new VanillaGossiper(address, chainAddress, clusterAddresses);
        voter = new VanillaVoter(this, clusterAddresses);
        voteTaker = new VanillaVoteTaker(messageListener, address, chainAddress, clusterAddresses);
        blockReplayer = new VanillaBlockReplayer<>(address, dtoRegistry, postBlockChainProcessor);
        String regionStr = DecentredUtil.toAddressString(chainAddress);
        votingSes = Executors.newSingleThreadExecutor(new NamedThreadFactory(regionStr + "-voter", true, Thread.MAX_PRIORITY));
        processingSes = Executors.newSingleThreadExecutor(new NamedThreadFactory(regionStr + "-processor", true, Thread.MAX_PRIORITY));
        writerSes = Executors.newCachedThreadPool(new NamedThreadFactory(regionStr + "-writer", true, Thread.MIN_PRIORITY));
    }

    public static BlockEngine newMain(long address, int periodMS, long[] clusterAddresses) {
        throw new UnsupportedOperationException();
/*
        assert LongStream.of(clusterAddresses).distinct().count() == clusterAddresses.length;

        final AddressService addressService = new AddressService();

        Chainer chainer = new QueuingChainer(CountryRegion.MAIN_CHAIN);
        AllMessagesServer fastPath = new MainFastPath(address, chainer, addressService);

        AllMessagesServer postBlockChainProcessor = new MainPostBlockChainProcessor(address, addressService);
        return new BlockEngine(address, CountryRegion.MAIN_CHAIN, periodMS, fastPath, chainer, postBlockChainProcessor, clusterAddresses);
*/
    }

    public static BlockEngine newLocal(long address, long region, int periodMS, long[] clusterAddresses, long tbeInitialCapacity) {
        throw new UnsupportedOperationException();
/*
        assert LongStream.of(clusterAddresses).distinct().count() == clusterAddresses.length;

        Chainer chainer = new VanillaChainer(region, tbeInitialCapacity);
        AllMessagesServer fastPath = new MainFastPath(address, chainer, null);

        AllMessagesServer postBlockChainProcessor = new LocalPostBlockChainProcessor(address);
        return new BlockEngine(address, region, periodMS, fastPath, chainer, postBlockChainProcessor, clusterAddresses);
*/
    }

    public void start() {
        votingSes.submit(this::runVoter);
        for (Runnable runnable : messageWriter.runnables()) {
            writerSes.submit(runnable);
        }
    }
/*
    @Override
    public void allMessagesLookup(AllMessagesLookup lookup) {
        super.allMessagesLookup(lookup);
        fastPath.allMessagesLookup(this);
        gossiper.allMessagesLookup(this);
        voter.allMessagesLookup(this);
        voteTaker.allMessagesLookup(this);
        messageWriter = new MultiMessageListener(2, (XCLServer) lookup);
//        messageWriter = new SingleMessageListener((XCLServer) lookup);
        postBlockChainProcessor.allMessagesLookup(messageWriter);
    }*/


/*
    @Override
    public void createAddressEvent(CreateAddressEvent createNewAddressCommand) {
        fastPath.createAddressEvent(createNewAddressCommand);
    }
*/


    @Override
    public void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent) {
        blockReplayer.transactionBlockEvent(transactionBlockEvent);
        gossiper.transactionBlockEvent(transactionBlockEvent);
    }

    @Override
    public void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent) {
        voter.transactionBlockGossipEvent(transactionBlockGossipEvent);
    }

    @Override
    public void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent) {
        voteTaker.transactionBlockVoteEvent(transactionBlockVoteEvent);
    }

    @Override
    public void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent) {
        blockReplayer.treeBlockEvent(endOfRoundBlockEvent);
    }

    void runVoter() {
        try {
//            Histogram one = new Histogram();
//            Histogram two = new Histogram();
//            Histogram three = new Histogram();
//            Histogram four = new Histogram();
            int count = 0;
            while (!Thread.currentThread().isInterrupted()) {
                long start = System.nanoTime();
                TransactionBlockEvent tbe = chainer.nextTransactionBlockEvent();
//                one.sample(System.nanoTime() - start);
                start = System.nanoTime();
                // tg System.out.println("TBE "+tbe);
                if (tbe != null) {
                    tbe.address(address);
                    tbe.blockNumber(blockNumber++);
                    for (long clusterAddress : clusterAddresses) {
                        messageListener.onMessage(clusterAddress, tbe);
                    }
                }

//                int subRound = Math.max(100_000, periodUS * 100);
//                two.sample(System.nanoTime() - start);
                Thread.sleep(1);
//                LockSupport.parkNanos(subRound);
                gossiper.sendGossip(blockNumber);
                Thread.sleep(1);
//                LockSupport.parkNanos(subRound);

                start = System.nanoTime();
                voter.sendVote(blockNumber);
//                three.sample(System.nanoTime() - start);

                Thread.sleep(1);
//                LockSupport.parkNanos(subRound);
                start = System.nanoTime();
                //System.out.println(address + " " + blockNumber);
                if (voteTaker.hasMajority()) {
                    voteTaker.sendEndOfRoundBlock(blockNumber++);
                }

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
                    System.out.println(one.toMicrosFormat());
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

    @Override
    public void close() {
        votingSes.shutdownNow();
    }

    public Chainer chainer() {
        return chainer;
    }
}
