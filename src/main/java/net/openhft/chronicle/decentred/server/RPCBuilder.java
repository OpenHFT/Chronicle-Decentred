package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.api.TransactionProcessor;
import net.openhft.chronicle.decentred.dto.address.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.MicroTimestampLongConverter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class RPCBuilder<U extends T, T> {
    private final Class<T> tClass;
    private final DtoRegistry<U> dtoRegistry;

    private KeyPair keyPair = new KeyPair();
    private Set<Long> clusterAddresses = new LinkedHashSet<>();
    private int mainBlockPeriodMS = 1000;
    private int localBlockPeriodMS = 100;
    private String region = "test";
    private boolean internal = false;

    private RPCBuilder(Class<U> uClass, Class<T> tClass) {
        this.tClass = tClass;
        dtoRegistry = DtoRegistry.newRegistry(uClass);
    }

    private RPCBuilder(int protocol, Class<U> uClass, Class<T> tClass) {
        this.tClass = tClass;
        dtoRegistry = DtoRegistry.newRegistry(protocol, uClass);
    }

    public static <U extends T, T> RPCBuilder<U, T> of(Class<U> uClass, Class<T> tClass) {
        return new RPCBuilder<>(uClass, tClass);
    }

    public static <U extends T, T> RPCBuilder<U, T> of(int protocol, Class<U> uClass, Class<T> tClass) {
        return new RPCBuilder<>(protocol, uClass, tClass);
    }

    public RPCServer<U, T> createServer(int port, T mainTransactionProcessor, T localTransactionProcessor, Function<GatewayConfiguration<U>, VanillaGateway> gatewayConstructor) throws IOException {
        return createServer("server:" + port, port, mainTransactionProcessor, localTransactionProcessor, gatewayConstructor, UniqueMicroTimeProvider.INSTANCE);
    }

    public RPCServer<U, T> createServer(String name, int port, Gateway gateway) throws IOException {

        final RPCServer<U, T> server = new RPCServer<>(
            name,
            port,
            keyPair,
            tClass,
            dtoRegistry,
            t -> (T) gateway
        ).internal(internal);

        return server;
    }

    public RPCServer<U, T> createServer(String name, int port, T mainTransactionProcessor, T localTransactionProcessor, Function<GatewayConfiguration<U>, VanillaGateway> gatewayConstructor, TimeProvider timeProvider) throws IOException {
        assert mainTransactionProcessor instanceof TransactionProcessor;
        assert localTransactionProcessor instanceof TransactionProcessor;

        long serverAddress = keyPair.address();
        addClusterAddress(serverAddress);

        boolean addressAdded = clusterAddresses.add(serverAddress);
        long[] clusterAddressArray =
                clusterAddresses.stream()
                        .mapToLong(i -> i)
                        .toArray();

        VanillaGateway gateway = gatewayConstructor.apply(GatewayConfiguration.of(
            dtoRegistry,
            keyPair,
            region,
            clusterAddressArray,
            mainBlockPeriodMS,
            localBlockPeriodMS,
            timeProvider
        ));

RPCServer<U, T> server = new RPCServer<>(
                name,
                port,
                keyPair,
                tClass,
                dtoRegistry,
                t -> (T) gateway
        ).internal(internal);
        ((TransactionProcessor) mainTransactionProcessor).messageRouter(server);
        ((TransactionProcessor) localTransactionProcessor).messageRouter(server);
        gateway.start(server);
        // register the address - otherwise, verify will fail
        gateway.createAddressEvent(
                new CreateAddressEvent()
                        .createAddressRequest(new CreateAddressRequest()
                                .publicKey(keyPair.publicKey)));

        if (addressAdded)
            clusterAddresses.remove(serverAddress);
        return server;
    }

    public RPCClient<U, T> createClient(String name, InetSocketAddress socketAddress, T allMessages) {
        return new RPCClient<>(name, socketAddress.getHostName(), socketAddress.getPort(), keyPair.secretKey, dtoRegistry, allMessages, tClass)
            .internal(internal);
    }

    public RPCClient<U, T> createAccountClient(String name, Bytes secretKey, InetSocketAddress socketAddress, T allMessages) {
        return new RPCClient<>(name, socketAddress.getHostName(), socketAddress.getPort(), secretKey, dtoRegistry, allMessages, tClass)
            .internal(true);
    }

    public RPCBuilder<U, T> addClusterAddress(long serverAddress) {
        clusterAddresses.add(serverAddress);
        return this;
    }

    public KeyPair keyPair() {
        return keyPair;
    }

    public RPCBuilder<U, T> keyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
        return this;
    }

    public Set<Long> clusterAddresses() {
        return clusterAddresses;
    }

    public RPCBuilder<U, T> clusterAddresses(Set<Long> clusterAddresses) {
        this.clusterAddresses = clusterAddresses;
        return this;
    }

    public int mainBlockPeriodMS() {
        return mainBlockPeriodMS;
    }

    public RPCBuilder<U, T> mainBlockPeriodMS(int mainBlockPeriodMS) {
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

    public RPCBuilder<U, T> region(String region) {
        this.region = region;
        return this;
    }

    public boolean internal() {
        return internal;
    }

    public RPCBuilder<U, T> internal(boolean internal) {
        this.internal = internal;
        return this;
    }
}
