package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.api.TransactionProcessor;
import net.openhft.chronicle.decentred.dto.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.CreateAddressRequest;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.salt.Ed25519;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class RPCBuilder<T> {
    private final Class<T> tClass;
    private final DtoRegistry<T> dtoRegistry;

    private BytesStore privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
    private BytesStore publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
    private BytesStore secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
    private Set<Long> clusterAddresses = new LinkedHashSet<>();
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

        if (publicKey.isEmpty() || secretKey.isEmpty()) {
            if (privateKey.isEmpty())
                Ed25519.generatePublicAndSecretKey((Bytes) publicKey, (Bytes) secretKey);
            else
                Ed25519.privateToPublicAndSecret((Bytes) publicKey, (Bytes) secretKey, privateKey);
        }
        long serverAddress = DecentredUtil.toAddress(publicKey);
        addClusterAddress(serverAddress);

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
        ((TransactionProcessor) mainTransactionProcessor).messageRouter(server);
        ((TransactionProcessor) localTransactionProcessor).messageRouter(server);
        gateway.start(server);
        // register the address - otherwise, verify will fail
        gateway.createAddressEvent(
                new CreateAddressEvent()
                        .createAddressRequest(new CreateAddressRequest()
                                .publicKey(publicKey)));

        if (addressAdded)
            clusterAddresses.remove(serverAddress);
        return server;
    }

   /* public RPCClient<T, T> createClient(String name, String hostname, int port, long serverAddress, T allMessages) {
        return new RPCClient(name, hostname, port, serverAddress, secretKey, allMessages)
                .internal(internal);
    }*/


    public RPCBuilder<T> addClusterAddress(long serverAddress) {
        clusterAddresses.add(serverAddress);
        return this;
    }

    public BytesStore publicKey() {
        return publicKey;
    }

    public RPCBuilder<T> publicKey(BytesStore publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public BytesStore secretKey() {
        return secretKey;
    }

    public RPCBuilder<T> secretKey(BytesStore secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public Set<Long> clusterAddresses() {
        return clusterAddresses;
    }

    public RPCBuilder<T> clusterAddresses(Set<Long> clusterAddresses) {
        this.clusterAddresses = clusterAddresses;
        return this;
    }

    public int mainBlockPeriodMS() {
        return mainBlockPeriodMS;
    }

    public RPCBuilder<T> mainBlockPeriodMS(int mainBlockPeriodMS) {
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

    public RPCBuilder<T> region(String region) {
        this.region = region;
        return this;
    }

    public boolean internal() {
        return internal;
    }

    public RPCBuilder<T> internal(boolean internal) {
        this.internal = internal;
        return this;
    }
}
