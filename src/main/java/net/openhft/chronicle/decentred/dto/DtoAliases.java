package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.core.pool.ClassAliasPool;

public enum DtoAliases {
    ;

    static {
        ClassAliasPool.CLASS_ALIASES.addAlias(
                ApplicationErrorResponse.class,
                CreateAddressRequest.class,
                CreateAddressEvent.class,
                InvalidationEvent.class,
                TransactionBlockEvent.class,
                TransactionBlockGossipEvent.class,
                TransactionBlockVoteEvent.class,
                EndOfRoundBlockEvent.class,
                VerificationEvent.class
        );
    }

    public static void addAliases() {
        // static init block does everything.
    }
}
