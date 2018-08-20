package town.lost.examples.exchange.api;

import net.openhft.chronicle.bytes.MethodId;
import town.lost.examples.exchange.dto.OpeningBalanceEvent;

public interface ValueGateway {
    @MethodId(0x0010)
    void openningBalanceEvent(OpeningBalanceEvent openingBalanceEvent);
}
