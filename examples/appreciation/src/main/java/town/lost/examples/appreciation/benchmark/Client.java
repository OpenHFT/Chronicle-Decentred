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

public class Client extends Node<AppreciationMessages, AppreciationResponses> implements AppreciationResponses {
    private final RPCClient<AppreciationMessages, AppreciationResponses> rpcClient;

    private Client(int seed, String serverHost, int serverPort) {
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
        System.out.println(address() + " onBalance = " + onBalance);
    }

    @Override
    public void verificationEvent(VerificationEvent verificationEvent) {
        System.out.println(address() + " verificationEvent = " + verificationEvent);
    }

    @Override
    public void invalidationEvent(InvalidationEvent invalidationEvent) {
        System.out.println(address() + " invalidationEvent = " + invalidationEvent);
    }

    public static void main(String[] args) throws InterruptedException {
        String serverHost = args[0];
        int seed = Integer.parseInt(args[1]);
        int otherSeed = Integer.parseInt(args[2]);

        long otherAddress = Client.addressFromSeed(otherSeed);

        Client client = new Client(seed, serverHost, Server.DEFAULT_SERVER_PORT);
        client.connect();
        OpeningBalance openingBalance = new OpeningBalance()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .init(client.address(), 100);
        client.sendMsg(openingBalance);

        Thread.sleep(5000);

        client.sendMsg(new Give()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .address(client.address())
            .init(otherAddress, seed));
    }
}