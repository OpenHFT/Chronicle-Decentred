package town.lost.examples.appreciation;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import town.lost.examples.appreciation.api.AppreciationMessages;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SingleMessageRoundtripTest {

    private static final int CLIENT_SEED = 1;
    private static final KeyPair KEY_PAIR = new KeyPair(CLIENT_SEED);

    private static final int OTHER_CLIENT_SEED = 2;
    private static final KeyPair OTHER_KEY_PAIR = new KeyPair(OTHER_CLIENT_SEED);

    private static final long CLIENT_ADDRESS = DecentredUtil.toAddress(KEY_PAIR.publicKey);
    private static final long OTHER_CLIENT_ADDRESS = DecentredUtil.toAddress(OTHER_KEY_PAIR.publicKey);

    private static final int MGMT_SEED = 3;
    private static final KeyPair MGMT_KEY_PAIR = new KeyPair(MGMT_SEED);

    private static final double START_AMOUNT = 100.0;
    private static final double GIVE_AMOUNT = 25.0;
    public static final int MAX_WAIT_TIME = 20_000;
    private static final double EPSILON = 0.0001;

    private final DtoRegistry<AppreciationMessages> registry;

    private BalanceStore balanceStore;
    private Server server;
    private Client client;

    SingleMessageRoundtripTest() {
        registry = DtoRegistry.newRegistry(17, AppreciationMessages.class);
    }

    @BeforeEach
    void setup() throws IOException, InterruptedException {
        balanceStore = new VanillaBalanceStore();
        server = new Server(42, 0, balanceStore);

        client = new Client(CLIENT_SEED, "0.0.0.0", server.getPort());
        Jvm.pause(5000); // Wait for server to come up // Todo: Fix this
        client.connect();

        setupBalance(CLIENT_ADDRESS, START_AMOUNT);
        setupBalance(OTHER_CLIENT_ADDRESS, START_AMOUNT);
    }

    @Test
    void testConnect() {
    }

    @Test
    void testBalance() {
        System.out.println(balanceStore);
        balanceEquals(CLIENT_ADDRESS, START_AMOUNT);
        balanceEquals(OTHER_CLIENT_ADDRESS, START_AMOUNT);
    }

    @Test
    void testGive() throws InterruptedException {
        Thread.sleep(1000);  // TODO - needed for account to be available for give

        final Give give = registry.create(Give.class)
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .address(CLIENT_ADDRESS)
            .init(OTHER_CLIENT_ADDRESS, GIVE_AMOUNT)
            .sign(KEY_PAIR.secretKey)
            ;

        client.sendMsg(give);

        waitFor(() -> balanceEquals(CLIENT_ADDRESS, START_AMOUNT - GIVE_AMOUNT));
        assertTrue(balanceEquals(OTHER_CLIENT_ADDRESS, START_AMOUNT + GIVE_AMOUNT));
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

    private boolean balanceEquals(long address, double balance) {
        Balances balances = balanceStore.getBalances(address);
        return balances != null && abs(balances.balance()-balance) < EPSILON;
    }

    private void setupBalance(long address, double amount) throws InterruptedException {
        System.out.format("setting balance for %s to %f%n", DecentredUtil.toAddressString(address), amount);
        final OpeningBalance openingBalance = registry.create(OpeningBalance.class)
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .init(address, amount)
            .sign(MGMT_KEY_PAIR.secretKey)
            ;
        final long start = System.nanoTime();
        client.sendMsg(openingBalance);
        waitFor(() -> balanceEquals(address, amount));
        final long durationMs = (System.nanoTime()- start)/1000000;
        System.out.format("Balance for %s set to %f took %,d ms%n", DecentredUtil.toAddressString(address), amount, durationMs);
    }
}
