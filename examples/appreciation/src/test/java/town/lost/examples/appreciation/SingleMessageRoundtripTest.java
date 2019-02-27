package town.lost.examples.appreciation;

import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import town.lost.examples.appreciation.benchmark.Client;
import town.lost.examples.appreciation.benchmark.Server;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OpeningBalance;
import town.lost.examples.appreciation.util.BalanceStore;
import town.lost.examples.appreciation.util.Balances;
import town.lost.examples.appreciation.util.VanillaBalanceStore;

import java.io.IOException;
import java.util.function.Supplier;

import static java.lang.Math.abs;

final class SingleMessageRoundtripTest {

    private static final int CLIENT_SEED = 1;
    private static final int OTHER_CLIENT_SEED = 2;

    private static final long CLIENT_ADDRESS = Client.addressFromSeed(CLIENT_SEED);
    private static final long OTHER_CLIENT_ADDRESS = Client.addressFromSeed(OTHER_CLIENT_SEED);


    private static final double START_AMOUNT = 100.0;
    private static final double GIVE_AMOUNT = 25.0;
    public static final int MAX_WAIT_TIME = 10_000;
    private static final double EPSILON = 0.0001;


    private BalanceStore balanceStore;
    private Server server;
    private Client client;

    @BeforeEach
    void setup() throws IOException, InterruptedException {
        balanceStore = new VanillaBalanceStore();
        server = new Server(42, 0, balanceStore);
        client = new Client(CLIENT_SEED, "0.0.0.0", server.getPort());
        client.connect();

        setupBalance(CLIENT_ADDRESS, START_AMOUNT);
        setupBalance(OTHER_CLIENT_ADDRESS, START_AMOUNT);
    }

    @Test
    void testConnect() {
    }

    @Test
    void testGive() throws InterruptedException {
        Thread.sleep(1000);  // TODO - needed for account to be available for give

        final Give give = new Give()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .address(CLIENT_ADDRESS)
            .init(OTHER_CLIENT_ADDRESS, GIVE_AMOUNT);

        client.sendMsg(give);

        waitFor(() -> assertBalance(CLIENT_ADDRESS, START_AMOUNT - GIVE_AMOUNT));
        assert assertBalance(OTHER_CLIENT_ADDRESS, START_AMOUNT + GIVE_AMOUNT);
    }


    @AfterEach
    void cleanup() {
        client.close();
        server.close();
    }

    private void waitFor(Supplier<Boolean> check) throws InterruptedException {
        long deadLine = System.currentTimeMillis() + MAX_WAIT_TIME;
        while(!check.get()) {
            Thread.sleep(100);
            if (System.currentTimeMillis() > deadLine) throw new InterruptedException("Hit deadline");
        }
    }

    private boolean assertBalance(long address, double balance) {
        Balances balances = balanceStore.getBalances(address);
        return balances != null && abs(balances.balance()-balance) < EPSILON;
    }

    private void setupBalance(long address, double amount) throws InterruptedException {
        OpeningBalance openingBalance = new OpeningBalance()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .init(address, amount);
        client.sendMsg(openingBalance);
        waitFor(() -> assertBalance(CLIENT_ADDRESS, amount));
    }
}
