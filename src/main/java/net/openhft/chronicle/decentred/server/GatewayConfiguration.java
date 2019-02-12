package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.util.DtoRegistry;

public interface GatewayConfiguration<U> {
    DtoRegistry<U> dtoRegistry();
    long address();
    String regionStr();
    long[] clusterAddresses();
    int mainPeriodMS();
    int localPeriodMS();

    static <T, U extends T> GatewayConfiguration<U> of (
        DtoRegistry<U> dtoRegistry,
        long serverAddress,
        String region,
        long[] clusterAddressArray,
        int mainBlockPeriodMS,
        int localBlockPeriodMS,
        T mainTransactionProcessor,
        T localTransactionProcessor
    ) {
        return new GatewayConfiguration<U>() {
            @Override
            public DtoRegistry<U> dtoRegistry() {
                return dtoRegistry;
            }

            @Override
            public long address() {
                return serverAddress;
            }

            @Override
            public String regionStr() {
                return region;
            }

            @Override
            public long[] clusterAddresses() {
                return clusterAddressArray;
            }

            @Override
            public int mainPeriodMS() {
                return mainBlockPeriodMS;
            }

            @Override
            public int localPeriodMS() {
                return localBlockPeriodMS;
            }
        };
    }
}
