package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.core.pool.ClassAliasPool;

public enum DtoAlias {
    ;

    static {
        ClassAliasPool.CLASS_ALIASES.addAlias(
                ApplicationErrorResponse.class,
                CreateAccountCommand.class,
                AccountCreatedResponse.class,
                VerificationEvent.class
        );
    }

    public static void addAliases() {
        // static init block does everything.
    }
}
