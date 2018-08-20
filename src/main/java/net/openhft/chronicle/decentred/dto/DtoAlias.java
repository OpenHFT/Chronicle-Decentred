package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.core.pool.ClassAliasPool;

public enum DtoAlias {
    ;

    static {
        ClassAliasPool.CLASS_ALIASES.addAlias(
                ApplicationErrorResponse.class,
                CreateAccountRequest.class,
                CreateAccountResponse.class,
                InvalidationEvent.class,
                TransactionBlockEvent.class,
                VerificationEvent.class
        );
    }

    public static void addAliases() {
        // static init block does everything.
    }
}
