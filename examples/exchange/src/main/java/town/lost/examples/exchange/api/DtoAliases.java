package town.lost.examples.exchange.api;

import net.openhft.chronicle.core.pool.ClassAliasPool;
import town.lost.examples.exchange.dto.*;

public enum DtoAliases {
    ;

    static {
        ClassAliasPool.CLASS_ALIASES.addAlias(
                CancelOrderEvent.class,
                CancelOrderRequest.class,
                NewOrderRequest.class,
                OpeningBalanceEvent.class,
                TradeEvent.class
        );
    }

    public static void addAliases() {
        net.openhft.chronicle.decentred.dto.DtoAliases.addAliases();
    }
}
