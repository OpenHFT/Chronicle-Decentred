package town.lost.examples.exchange.api;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.SystemMessageListener;

public interface ExchangeOut extends SystemMessageListener, MessageRouter<ExchangeResponses> {
}
