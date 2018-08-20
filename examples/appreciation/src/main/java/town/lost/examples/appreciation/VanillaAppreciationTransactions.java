package town.lost.examples.appreciation;

import net.openhft.chronicle.decentred.api.MessageRouter;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.api.AppreciationTransactions;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OnBalance;
import town.lost.examples.appreciation.dto.OpeningBalance;

/**
 * Run from the blockchain.
 */
public class VanillaAppreciationTransactions implements AppreciationTransactions {
    private final MessageRouter<AppreciationResponses> router;
    private final BalanceStore balanceStore;
    private final OnBalance onBalance = new OnBalance();

    public VanillaAppreciationTransactions(
            MessageRouter<AppreciationResponses> router,
            BalanceStore balanceStore) {
        this.router = router;
        this.balanceStore = balanceStore;
    }

    @Override
    public void openingBalance(OpeningBalance openingBalance) {
        balanceStore.setBalance(openingBalance.balanceAddress(), openingBalance.amount());
    }

    @Override
    public void give(Give give) {
        long fromKey = give.address();
        long toKey = give.toAddress();
        if (balanceStore.subtractBalance(fromKey, give.amount())) {
            balanceStore.addBalance(toKey, give.amount());
            onBalance.timestampUS(give.timestampUS());

            router.to(fromKey)
                    .onBalance(onBalance.init(fromKey, balanceStore.getBalance(fromKey)));
            router.to(toKey)
                    .onBalance(onBalance.init(toKey, balanceStore.getBalance(toKey)));
        }
    }
}
