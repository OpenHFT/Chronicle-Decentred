package town.lost.examples.appreciation.api;

import net.openhft.chronicle.decentred.api.ConnectionStatusListener;

/**
 * Gateway processor which handles queries from the client
 * as well as validate and pass transactions
 */
public interface AppreciationGateway extends
        AppreciationRequests,
        ConnectionStatusListener {

}
