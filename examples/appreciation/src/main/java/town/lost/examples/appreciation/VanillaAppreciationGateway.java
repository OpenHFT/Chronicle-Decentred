package town.lost.examples.appreciation;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.dto.ApplicationErrorResponse;
import net.openhft.chronicle.decentred.dto.CreateAccountRequest;
import town.lost.examples.appreciation.api.AppreciationGateway;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.api.AppreciationTransactions;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OnBalance;
import town.lost.examples.appreciation.dto.OpeningBalance;
import town.lost.examples.appreciation.dto.QueryBalance;


/**
 * Run as a gateway before the blockchain.
 */
public class VanillaAppreciationGateway implements AppreciationGateway {
    private final MessageRouter<AppreciationResponses> client;
    private final AppreciationTransactions blockchain;
    private final BalanceStore balanceStore;

    private final OnBalance onBalance = new OnBalance();
    private final ApplicationErrorResponse error = new ApplicationErrorResponse();

    public VanillaAppreciationGateway(
            MessageRouter<AppreciationResponses> client,
            AppreciationTransactions blockchain,
            BalanceStore balanceStore) {
        this.client = client;
        this.blockchain = blockchain;
        this.balanceStore = balanceStore;
    }

    @Override
    public void openingBalance(OpeningBalance openingBalance) {
        if (verifyServerNode(openingBalance.address()))
            blockchain.openingBalance(openingBalance);
    }

    private boolean verifyServerNode(long address) {
        // allow anyone for now.
        return true;
    }

    @Override
    public void queryBalance(QueryBalance queryBalance) {
        AppreciationResponses listener = client.to(queryBalance.address());
        double amount = balanceStore.getBalance(queryBalance.address());
        if (Double.isNaN(amount)) {
            error.init(queryBalance,
                    "Cannot query balance: Account doesn't exist");
            error.timestampUS(queryBalance.timestampUS());
            listener.applicationError(error);
        } else {
            onBalance.timestampUS(queryBalance.timestampUS());
            listener.onBalance(onBalance);
        }
    }

    @Override
    public void give(Give give) {
        if (give.amount() < 0) {
            AppreciationResponses listener = client.to(give.toAddress());
            error.init(give,
                    "Cannot give a negative amount");
            error.timestampUS(give.timestampUS());
            listener.applicationError(error);
            return;
        }
        double amount = balanceStore.getBalance(give.address());
        if (Double.isNaN(amount)) {
            AppreciationResponses listener = client.to(give.address());
            error.init(give,
                    "Cannot give balance: Account doesn't exist");
            error.timestampUS(give.timestampUS());
            listener.applicationError(error);
            return;
        }

        blockchain.give(give);
    }

    @Override
    public void createAccountRequest(CreateAccountRequest createAccountRequest) {

    }
}
