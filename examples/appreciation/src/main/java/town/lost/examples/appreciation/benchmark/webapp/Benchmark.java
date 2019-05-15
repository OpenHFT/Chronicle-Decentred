package town.lost.examples.appreciation.benchmark.webapp;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import town.lost.examples.appreciation.benchmark.Traffic;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OpeningBalance;
import town.lost.examples.appreciation.dto.QueryBalance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
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
    private final AtomicBoolean running;

    private long start;
    private int iterations;
    //
    private boolean connected;
    private Traffic.Client firstClient;
    private Traffic.Client secondClient;

    // Todo: remove these
    private double firstBalance;
    private double secondBalance;


    Benchmark(
        final InetSocketAddress socketAddress,
        final long firstSeed,
        final long secondSeed,
        final double firstInitialValue,
        final double secondInitialValue,
        final int totalIterations,
        final Consumer<String> logListener,
        final Consumer<Boolean> connectedListener
    ) {
        super(Benchmark.class.getSimpleName());
        this.socketAddress = requireNonNull(socketAddress);
        this.firstSeed = firstSeed;
        this.firstInitialValue = firstInitialValue;
        this.secondSeed = secondSeed;
        this.secondInitialValue = secondInitialValue;
        this.logListener = requireNonNull(logListener);
        this.connectedListener = requireNonNull(connectedListener);

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

        log("Preparing list of messages");
        List<Give> giveList = IntStream.range(0, totalIterations)
            .mapToObj(i ->
                new Give()
                    .address(firstClient.address())
                    .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                    .init(secondClient.address(), 1)
            )
            .collect(toList());

        log("Benchmark with " + totalIterations + " iterations started.");
        this.start = System.nanoTime(); // Reset start
        while (running.get() && !Thread.interrupted() && !isCompleted()) {
            while (!isCompleted()) {
                if (firstBalance() > 0 && secondBalance() > 0) { // Todo: Remove this condition when real read of balance is done
                    firstBalance--;
                    secondBalance++;
                }
                firstClient.toDefault().give(giveList.get(iterations));
                if ((iterations++ % 10000) == 0 && iterations != 1) {
                    log(Integer.toString(iterations - 1));
                    break; // Check regularly
                }
            }
        }
        connectedListener.accept(false);
        firstClient.close();
        secondClient.close();
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
        return iterations;
    }

    float progress() {
        return ((float) iterations()) / ((float) totalIterations());
    }

    double tps() {
        return 1000f * ((double) iterations()) / ((double) elapsedMs());
    }

    boolean isCompleted() {
        return iterations >= totalIterations;
    }

    void shutDown() {
        running.set(false);
    }

    private void log(String s) {
        logListener.accept(s);
    }

    private void sendCreateAddress(Traffic.Client client) {
        log("Creating address " + client.address());
        client.toDefault().createAddressRequest(new CreateAddressRequest()
            .address(client.address())
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros()));
    }

    private void sendOpeningBalance(Traffic.Client client, double balance) {
        log("Setting opening balance for " + client.address() + " to " + balance);
        client.toDefault().openingBalance(new OpeningBalance()
            .address(client.address())
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .init(client.address(), balance));
    }

    private double retrieveSaldo(Traffic.Client client) {
        QueryBalance queryBalance = new QueryBalance()
            .address(client.address())
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros());

        client.toDefault().queryBalance(queryBalance);
        return 0d;
    }



}
