package town.lost.examples.appreciation.api;

import net.openhft.chronicle.bytes.MethodId;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OpeningBalance;

/**
 * Transactions passed through the block chain
 */
public interface AppreciationTransactions {

    /**
     * Report the current balance for this public key.
     */
    @MethodId(0x0001)
    void openingBalance(OpeningBalance openingBalance);

    @MethodId(0x0010)
    void give(Give give);
}
