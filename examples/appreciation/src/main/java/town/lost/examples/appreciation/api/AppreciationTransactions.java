package town.lost.examples.appreciation.api;

import net.openhft.chronicle.bytes.MethodId;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OpeningBalance;
import town.lost.examples.appreciation.dto.Topup;

/**
 * Transactions passed through the block chainevent
 */
public interface AppreciationTransactions {

    /**
     * Report the current balance for this public key.
     */
    @MethodId(0x0001)
    void openingBalance(OpeningBalance openingBalance);

    @MethodId(0x0010)
    void give(Give give);

    @MethodId(0x0020)
    void topup(Topup topup);
}
