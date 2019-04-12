package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;

public interface GatewayConfiguration<U> {
    DtoRegistry<U> dtoRegistry();
    KeyPair keyPair();
    String regionStr();
    long[] clusterAddresses();
    int mainPeriodMS();
    int localPeriodMS();
    TimeProvider timeProvider();

    static <T, U extends T> GatewayConfiguration<U> of (
        DtoRegistry<U> dtoRegistry,
        KeyPair keyPair,
        String region,
        long[] clusterAddressArray,
        int mainBlockPeriodMS,
        int localBlockPeriodMS,
        TimeProvider timeProvider
    ) {
        return new GatewayConfiguration<U>() {
            @Override
            public DtoRegistry<U> dtoRegistry() {
                return dtoRegistry;
            }

            @Override
            public KeyPair keyPair() {
                return keyPair;
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

            @Override
            public TimeProvider timeProvider() {
                return timeProvider;
            }
        };
    }

}
