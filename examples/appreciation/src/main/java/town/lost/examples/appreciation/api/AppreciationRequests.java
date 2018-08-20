package town.lost.examples.appreciation.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.api.AccountManagementRequests;
import town.lost.examples.appreciation.dto.QueryBalance;

/**
 * Transactions passed through the block chain
 */
public interface AppreciationRequests extends
        AccountManagementRequests,
        AppreciationTransactions {

    /**
     * Report the current balance for this public key.
     */
    @MethodId(0x0020)
    void queryBalance(QueryBalance queryBalance);
}
