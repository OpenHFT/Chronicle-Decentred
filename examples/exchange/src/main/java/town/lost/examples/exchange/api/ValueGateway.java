package town.lost.examples.exchange.api;

import net.openhft.chronicle.bytes.MethodId;
import town.lost.examples.exchange.dto.OpeningBalance;

public interface ValueGateway {
    @MethodId(0x0010)
    void openningBalance(OpeningBalance openingBalance);
}
