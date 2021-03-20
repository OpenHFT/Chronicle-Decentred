package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.internal.server.VanillaBlockEngine;
import net.openhft.chronicle.decentred.server.trait.HasTcpMessageListener;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import org.jetbrains.annotations.NotNull;

import java.util.stream.LongStream;

public interface BlockEngine extends SystemMessages, MessageListener, HasTcpMessageListener {

    void start(MessageToListener messageToListener);

    // Used for testing.
    void processOneBlock();

    static <T, U extends T> BlockEngine newMain(@NotNull DtoRegistry<U> dtoRegistry,
                                                KeyPair keyPair,
                                                int periodMS,
                                                long[] clusterAddresses,
                                                @NotNull T postBlockChainProcessor,
                                                @NotNull TimeProvider timeProvider) {

        assert LongStream.of(clusterAddresses).distinct().count() == clusterAddresses.length;
        long main = DecentredUtil.parseAddress("main");
        return new VanillaBlockEngine<>(dtoRegistry, keyPair, main, periodMS, postBlockChainProcessor, clusterAddresses, timeProvider);
    }

    static <T, U extends T> BlockEngine newLocal(@NotNull DtoRegistry<U> dtoRegistry,
                                                 KeyPair keyPair,
                                                 long chainAddress,
                                                 int periodMS,
                                                 long[] clusterAddresses,
                                                 @NotNull T postBlockChainProcessor,
                                                 @NotNull TimeProvider timeProvider) {

        assert LongStream.of(clusterAddresses).distinct().count() == clusterAddresses.length;
        return new VanillaBlockEngine<>(dtoRegistry, keyPair, chainAddress, periodMS, postBlockChainProcessor, clusterAddresses, timeProvider);
    }
}
