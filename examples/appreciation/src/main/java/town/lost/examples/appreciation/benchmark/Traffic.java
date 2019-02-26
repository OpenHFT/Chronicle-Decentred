package town.lost.examples.appreciation.benchmark;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.server.*;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.salt.Ed25519;
import town.lost.examples.appreciation.api.AppreciationMessages;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.dto.*;

import java.net.InetSocketAddress;
import java.util.stream.Stream;

public class Traffic  {
    private static final double START_AMOUNT = 2_000_000d;

    public static final long GIVER = 1;
    public static final long TAKER = 2;
    private static final long[] ACCOUNTS = {GIVER, TAKER};

    public Traffic() {}

    public static void main(String[] args) {
        final String[] addrPair = args[0].split(":");
        final InetSocketAddress socketAddress = InetSocketAddress.createUnresolved(addrPair[0], Integer.parseInt(addrPair[1]));
        System.out.println("Connecting to Gateway at " + socketAddress);

        Stream.of(GIVER/*ACCOUNTS*/).forEachOrdered(accountSeed -> {
            System.out.println("Setting up keys");

            final BytesStore privateKey = DecentredUtil.testPrivateKey(accountSeed);
            final Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
            final Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
            Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);
            System.out.println("Seed " + accountSeed + " is " + DecentredUtil.toAddressString(accountSeed));

            long address = DecentredUtil.toAddress(publicKey); // Isn't this the address to use?
            System.out.println("Setting RPC client");
            RPCClient<AppreciationMessages, AppreciationResponses> client =
                RPCBuilder.of(17, AppreciationMessages.class, AppreciationResponses.class)
                    .secretKey(secretKey)
                    .publicKey(publicKey)
                    .createClient(DecentredUtil.toAddressString(address), socketAddress, new Peer.ResponseSink());

            System.out.println("Waiting for ability to send first message...");
            Jvm.pause(7000);
            System.out.println("Sending CreateAddressRequest");

            client.write(new CreateAddressRequest()
                .address(address)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .publicKey(publicKey)
            );
            System.out.println("Waiting for ability to send second message...");
            Jvm.pause(7000);

            System.out.println("Setting account " + accountSeed + " to " + START_AMOUNT);

            final OpeningBalance ob = new OpeningBalance()
                .address(address)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .init(address, START_AMOUNT);
            client.write(ob);  // when signing this, the address is garbled!

            System.out.println("Waiting");
            Jvm.pause(2000);
            System.out.println("Closing");
            client.close();

        });

        /*

        Thread.sleep(10000);


        RPCClient<AppreciationMessages, AppreciationRequests> client = getRpcBuilder()
            .createAccountClient(GIVER, secretKeys.get(GIVER), socketAddress, new ResponseSink());

                client.write(new Give()
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .address(DecentredUtil.parseAddress(GIVER))
            .init(DecentredUtil.parseAddress(TAKER), 10));

        client.close();*/

/*
        while (System.in.available() == 0) {
            Thread.sleep(5000);
        }
        System.exit(0);*/

    }


}