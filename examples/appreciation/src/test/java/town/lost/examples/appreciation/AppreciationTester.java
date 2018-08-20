package town.lost.examples.appreciation;

import net.openhft.chronicle.decentred.api.MessageRouter;
import town.lost.examples.appreciation.api.AppreciationRequests;
import town.lost.examples.appreciation.api.AppreciationResponses;

/**
 * Combining interface for all messages
 */
public interface AppreciationTester extends
        MessageRouter<AppreciationResponses>,

        AppreciationResponses,
        AppreciationRequests {
}
