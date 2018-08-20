package town.lost.examples.exchange.api;

import net.openhft.chronicle.core.pool.ClassAliasPool;
import town.lost.examples.exchange.dto.CancelOrderRequest;
import town.lost.examples.exchange.dto.NewOrderRequest;
import town.lost.examples.exchange.dto.OpeningBalanceEvent;

public enum DtoAliases {
    ;

    static {
        ClassAliasPool.CLASS_ALIASES.addAlias(
                CancelOrderRequest.class,
                NewOrderRequest.class,
                OpeningBalanceEvent.class
        );
    }

    public static void addAliases() {
        net.openhft.chronicle.decentred.dto.DtoAliases.addAliases();
    }
}
