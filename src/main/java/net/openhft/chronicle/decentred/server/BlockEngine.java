package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.api.MessageListener;
import net.openhft.chronicle.decentred.api.MessageToListener;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.internal.server.VanillaBlockEngine;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.stream.LongStream;

public interface BlockEngine extends SystemMessages, MessageListener {

    void start(MessageToListener messageToListener);

    // Used for testing.
    void tcpMessageListener(MessageToListener messageToListener);

    // Used for testing.
    void processOneBlock();

    static <T, U extends T> BlockEngine newMain(@NotNull DtoRegistry<U> dtoRegistry,
                                                long address,
                                                int periodMS,
                                                long[] clusterAddresses,
                                                @NotNull T postBlockChainProcessor,
                                                @NotNull BytesStore secretKey) {

        assert LongStream.of(clusterAddresses).distinct().count() == clusterAddresses.length;
        long main = DecentredUtil.parseAddress("main");
        return new VanillaBlockEngine<>(dtoRegistry, address, main, periodMS, postBlockChainProcessor, clusterAddresses, secretKey);
    }

    static <T, U extends T> BlockEngine newLocal(@NotNull DtoRegistry<U> dtoRegistry,
                                                 long address,
                                                 long chainAddress,
                                                 int periodMS,
                                                 long[] clusterAddresses,
                                                 @NotNull T postBlockChainProcessor,
                                                 @NotNull BytesStore secretKey) {

        assert LongStream.of(clusterAddresses).distinct().count() == clusterAddresses.length;
        return new VanillaBlockEngine<>(dtoRegistry, address, chainAddress, periodMS, postBlockChainProcessor, clusterAddresses, secretKey);
    }

}
