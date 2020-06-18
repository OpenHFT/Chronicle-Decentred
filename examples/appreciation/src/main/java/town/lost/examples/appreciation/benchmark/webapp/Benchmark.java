package town.lost.examples.appreciation.benchmark.webapp;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import town.lost.examples.appreciation.api.AppreciationMessages;
import town.lost.examples.appreciation.benchmark.Traffic;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OpeningBalance;
import town.lost.examples.appreciation.dto.QueryBalance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static town.lost.examples.appreciation.benchmark.Traffic.createClient;

final class Benchmark extends Thread {

    private final InetSocketAddress socketAddress;
    private final long firstSeed;
    private final long secondSeed;
    private final double firstInitialValue;
    private final double secondInitialValue;
    private final int totalIterations;
    private final Consumer<String> logListener;
    private final Consumer<Boolean> connectedListener;
    private final Consumer<String> progressListener;
    private final AtomicBoolean running;

    private long start;
    private int iterations;
    //
    private boolean connected;
    private Traffic.Client firstClient;
    private Traffic.Client secondClient;

    private WorkerThread[] workerThreads;

    // Todo: remove these
    private double firstBalance;
    private double secondBalance;

    private int messageType;
    private int protocol;

Benchmark(
        final InetSocketAddress socketAddress,
        final long firstSeed,
        final long secondSeed,
        final double firstInitialValue,
        final double secondInitialValue,
        final int totalIterations,
        final Consumer<String> logListener,
        final Consumer<Boolean> connectedListener,
        final Consumer<String> progressListener
    ) {
        super(Benchmark.class.getSimpleName());
        this.socketAddress = requireNonNull(socketAddress);
        this.firstSeed = firstSeed;
        this.firstInitialValue = firstInitialValue;
        this.secondSeed = secondSeed;
        this.secondInitialValue = secondInitialValue;
        this.logListener = requireNonNull(logListener);
        this.connectedListener = requireNonNull(connectedListener);
        this.progressListener = requireNonNull(progressListener);

        this.running = new AtomicBoolean(true);
        this.totalIterations = totalIterations;
        this.start = System.nanoTime();

        this.firstBalance = firstInitialValue;
        this.secondBalance = secondInitialValue;
    }

    @Override
    public void run() {
        log("Connecting to Gateway at " + socketAddress);
        firstClient = createClient(firstSeed, socketAddress);
        secondClient = createClient(secondSeed, socketAddress);
        sendCreateAddress(firstClient);
        sendCreateAddress(secondClient);
        connectedListener.accept(true);
        Jvm.pause(500);
        sendOpeningBalance(firstClient, firstInitialValue);
        sendOpeningBalance(secondClient, secondInitialValue);
        connected = true;
        Jvm.pause(500);
        firstClient.close();
        secondClient.close();

        log("Preparing list of messages");
        progressListener.accept("Preparing messages");
        Map<Long, List<Give>> giveList = IntStream.range(0, totalIterations)
            .parallel()
            .mapToObj(i ->
                new Give()
                    .address(firstClient.address())
                    .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                    .init(secondClient.address(), 1)
                    /*
                    .protocol(protocol)
                    .messageType(messageType)
                    .sign(firstClient.secretKey())*/
            )
            .collect(groupingBy(g -> Thread.currentThread().getId()));

        workerThreads = giveList.values().stream()
            .map(WorkerThread::new)
            .toArray(WorkerThread[]::new);

        log("Benchmark with " + totalIterations + " iterations and " + workerThreads.length + " threads started.");
        progressListener.accept("Running benchmark");

        Stream.of(workerThreads).forEach(WorkerThread::start);

this.start = System.nanoTime(); // Reset start
        while (running.get() && !Thread.interrupted() && !isCompleted()) {
/*            while (!isCompleted()) {
                kickBalance(); // Todo: Remove this condition when real read of balance is done
                firstClient.toDefault().give(giveList.get(iterations));
                if ((iterations++ % 10000) == 0 && iterations != 1) {
                    log(Integer.toString(iterations - 1));
                    break; // Check regularly
                }
            }*/
            Jvm.pause(500);
            log(Benchmark.class.getSimpleName() + " alive with " + iterations() + " iterations");
        }
        log(Benchmark.class.getSimpleName() + " completed");
        Stream.of(workerThreads).forEach(WorkerThread::shutDown);
        connectedListener.accept(false);
        log(String.format("Benchmark completed %,d iterations in %,d ms (%,.0f TPS)", iterations, elapsedMs(), tps()));
    }

    double firstBalance() {
        return firstBalance;
    }

    double secondBalance() {
        return secondBalance;
    }

    long elapsedMs() {
        return (System.nanoTime() - start) / 1_000_000L;
    }

    int totalIterations() {
        return totalIterations;
    }

    int iterations() {
        return  workerThreads == null
        ? 0
        :Stream.of(workerThreads).mapToInt(WorkerThread::iterations).sum();
    }

    float progress() {
        return ((float) iterations()) / ((float) totalIterations());
    }

    double tps() {
        return 1000f * ((double) iterations()) / ((double) elapsedMs());
    }

    boolean isCompleted() {
        return workerThreads == null
            ? false
            : Stream.of(workerThreads).allMatch(WorkerThread::completed);
    }

    void shutDown() {
        running.set(false);
    }

    private void log(String s) {
        logListener.accept(s);
    }

    public synchronized void kickBalance() {
        if (firstBalance > 0 && secondBalance > 0) { // Todo: Remove this condition when real read of balance is done
            firstBalance--;
            secondBalance++;
        }
    }

private void sendCreateAddress(Traffic.Client client) {
        log("Creating address " + client.address());
        client.toDefault().createAddressRequest(new CreateAddressRequest()
            .address(client.address())
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros()));
    }

    private void sendOpeningBalance(Traffic.Client client, double balance) {
        log("Setting opening balance for " + client.address() + " to " + balance);
        OpeningBalance ob = new OpeningBalance()
            .address(client.address())
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .init(client.address(), balance);

        client.toDefault().openingBalance(ob);

        messageType = ob.messageType();
        protocol = ob.protocol();

        System.out.println("messageType = " + messageType);
        System.out.println("protocol = " + protocol);
    }

    private double retrieveSaldo(Traffic.Client client) {
        QueryBalance queryBalance = new QueryBalance()
            .address(client.address())
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros());

        client.toDefault().queryBalance(queryBalance);
        return 0d;
    }

private final class WorkerThread extends Thread {

        private final List<Give> giveList;
        private final Traffic.Client c0;
        private final Traffic.Client c1;
        private final AtomicBoolean running;
        private final AtomicInteger i;
        private final AtomicBoolean completed;

        public WorkerThread(List<Give> giveList) {
            c0 = createClient(firstSeed, socketAddress);
            c1 = createClient(secondSeed, socketAddress);
            this.giveList = requireNonNull(giveList);
            running =  new AtomicBoolean(true);
            i = new AtomicInteger();
            completed = new AtomicBoolean();
        }

        @Override
        public void run() {
            log("Starting " + getName() + " with " + giveList.size() + " messages");
            final int end = giveList.size();
            final AppreciationMessages m = c0.toDefault();
            while (!Thread.interrupted() && running.get() && i.get() < end) {
                kickBalance(); // Todo: Remove this later
                final int j = i.getAndIncrement();
                m.give(giveList.get(j));
                if ((j % 10000) == 0 && j != 0) {
                    log(getName() + " is on "+ j);
                }
            }
            log(getName() + " completed " + i.get() + " iterations");
            c0.close();
            c1.close();
            running.set(false);
            completed.set(true);
        }

        void shutDown() {
            running.set(false);
        }

        int iterations() {
            return i.get();
        }

        boolean completed() {
            return completed.get();
        }
 }

}
