package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.decentred.api.TransactionProcessor;
import net.openhft.chronicle.decentred.dto.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.CreateAddressRequest;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.salt.Ed25519;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class RPCBuilder<T> {
    private final Class<T> tClass;
    private final DtoRegistry<T> dtoRegistry;

    private Bytes privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
    private Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
    private Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
    private Set<Long> clusterAddresses = new LinkedHashSet<>();
    private long serverAddress = 0;
    private int mainBlockPeriodMS = 1000;
    private int localBlockPeriodMS = 100;
    private String region = "test";
    private boolean internal = false;

    private RPCBuilder(Class<T> tClass) {
        this.tClass = tClass;
        dtoRegistry = DtoRegistry.newRegistry(tClass);
    }

    public static <T> RPCBuilder<T> of(Class<T> tClass) {
        return new RPCBuilder<>(tClass);
    }

    public RPCServer<T> createServer(int port, T mainTransactionProcessor, T localTransactionProcessor) throws IOException {
        return createServer("server:" + port, port, mainTransactionProcessor, localTransactionProcessor);
    }

    public RPCServer<T> createServer(String name, int port, T mainTransactionProcessor, T localTransactionProcessor) throws IOException {
        assert mainTransactionProcessor instanceof TransactionProcessor;
        assert localTransactionProcessor instanceof TransactionProcessor;
        long serverAddress = this.serverAddress;
        if (serverAddress == 0)
            serverAddress = port;

        if (publicKey.isEmpty() || secretKey.isEmpty()) {
            if (privateKey.isEmpty())
                Ed25519.generatePublicAndSecretKey(publicKey, secretKey);
            else
                Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);
        }

        boolean addressAdded = clusterAddresses.add(serverAddress);
        long[] clusterAddressArray =
                clusterAddresses.stream()
                        .mapToLong(i -> i)
                        .toArray();

        VanillaGateway gateway = VanillaGateway.newGateway(
                dtoRegistry,
                serverAddress,
                region,
                clusterAddressArray,
                mainBlockPeriodMS,
                localBlockPeriodMS,
                mainTransactionProcessor,
                localTransactionProcessor);
        RPCServer<T> server = new RPCServer<>(
                name,
                port,
                serverAddress,
                publicKey,
                secretKey,
                tClass,
                dtoRegistry,
                t -> (T) gateway)
                .internal(internal);
        gateway.start(server);
        // register the address - otherwise, verify will fail
        gateway.createAccountEvent(
                new CreateAddressEvent()
                        .createAccountRequest(new CreateAddressRequest()
                                .publicKey(publicKey)));

        if (addressAdded)
            clusterAddresses.remove(serverAddress);
        return server;
    }

   /* public RPCClient<T, T> createClient(String name, String hostname, int port, long serverAddress, T allMessages) {
        return new RPCClient(name, hostname, port, serverAddress, secretKey, allMessages)
                .internal(internal);
    }*/

    public RPCBuilder serverAddress(long serverAddress) {
        this.serverAddress = serverAddress;
        clusterAddresses.add(serverAddress);
        return this;
    }

    public RPCBuilder addClusterAddress(long serverAddress) {
        clusterAddresses.add(serverAddress);
        return this;
    }

    public Bytes publicKey() {
        return publicKey;
    }

    public RPCBuilder publicKey(Bytes publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public Bytes secretKey() {
        return secretKey;
    }

    public RPCBuilder secretKey(Bytes secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public Set<Long> clusterAddresses() {
        return clusterAddresses;
    }

    public RPCBuilder clusterAddresses(Set<Long> clusterAddresses) {
        this.clusterAddresses = clusterAddresses;
        return this;
    }

    public long serverAddress() {
        return serverAddress;
    }

    public int mainBlockPeriodMS() {
        return mainBlockPeriodMS;
    }

    public RPCBuilder mainBlockPeriodMS(int mainBlockPeriodMS) {
        this.mainBlockPeriodMS = mainBlockPeriodMS;
        return this;
    }

    public int localBlockPeriodMS() {
        return localBlockPeriodMS;
    }

    public RPCBuilder localBlockPeriodMS(int localBlockPeriodMS) {
        this.localBlockPeriodMS = localBlockPeriodMS;
        return this;
    }

    public String region() {
        return region;
    }

    public RPCBuilder region(String region) {
        this.region = region;
        return this;
    }

    public boolean internal() {
        return internal;
    }

    public RPCBuilder internal(boolean internal) {
        this.internal = internal;
        return this;
    }
}
