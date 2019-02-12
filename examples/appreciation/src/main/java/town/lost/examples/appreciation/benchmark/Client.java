package town.lost.examples.appreciation.benchmark;

import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import town.lost.examples.appreciation.api.AppreciationMessages;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OnBalance;
import town.lost.examples.appreciation.dto.OpeningBalance;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Client extends Node<AppreciationMessages, AppreciationResponses> implements AppreciationResponses {

    private static final double START_AMOUNT = 10_000_000d;
    private static final int ITERATIONS = 1_000_000;

    private enum Stage {WARMUP, MEASURE};

    private final RPCClient<AppreciationMessages, AppreciationResponses> rpcClient;

    Client(int seed, String serverHost, int serverPort) {
        super(seed, AppreciationMessages.class, AppreciationResponses.class);
        DtoRegistry<AppreciationMessages> dtoRegistry = DtoRegistry.newRegistry(17, AppreciationMessages.class);
        rpcClient = new RPCClient<>("test", serverHost, serverPort, getSecretKey(), dtoRegistry, this, AppreciationResponses.class);
    }

    public void connect() {
        sendMsg(new CreateAddressRequest()
            .address(0)
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .publicKey(getPublicKey()));
    }

    public void sendMsg(VanillaSignedMessage msg) {
        rpcClient.write(msg);
    }

    @Override
    public void onBalance(OnBalance onBalance) {
        // System.out.println(address() + " onBalance = " + onBalance);
    }

    @Override
    public void verificationEvent(VerificationEvent verificationEvent) {
        System.out.println(address() + " verificationEvent = " + verificationEvent);
    }

    @Override
    public void invalidationEvent(InvalidationEvent invalidationEvent) {
        System.out.println(address() + " invalidationEvent = " + invalidationEvent);
    }

    @Override
    public void close() {
        rpcClient.close();
    }

    public static void main(String[] args) {
        final String serverHost = args[0];
        final int seed = Integer.parseInt(args[1]);
        final int otherSeed = Integer.parseInt(args[2]);

        final long otherAddress = Client.addressFromSeed(otherSeed);

        final Client client = new Client(seed, serverHost, Server.DEFAULT_SERVER_PORT);
        client.connect();
        System.out.println("Client " + seed + " started with address " + client.address());

/*
        final OpeningBalance openingBalance = new OpeningBalance()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .init(client.address(), START_AMOUNT * seed);
        client.sendMsg(openingBalance);
*/

/*        waitForKey("Make sure all other clients are up"); */

        final Give give = new Give()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .address(client.address())
            .init(otherAddress, seed);

        /*client.sendMsg(give);*/

        waitForKey("Start benchmark.");

        for (Stage stage: Stage.values()) {
            System.out.println("Benchmarking Give: stage " + stage);
            final long start = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                give.timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros());
                client.sendMsg(give);
                if (i % 10000 == 0) {
                    System.out.print(".");
                }
            }
            final long duration = System.nanoTime() - start;
            final double tps = (double) ITERATIONS / ((double) duration / 1e9);
            System.out.format("%n%s %,d iterations took %,9d ns (%,6.0f tps)%n%n", stage, ITERATIONS, duration, tps);
        }


    }

    private static void waitForKey(String s) {
        System.out.println(s + " Press <return> to continue.");
        try {
            System.in.read();
        } catch (IOException ioe) {

        }
    }

}