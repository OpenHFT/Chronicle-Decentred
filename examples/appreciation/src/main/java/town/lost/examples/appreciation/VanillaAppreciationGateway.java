package town.lost.examples.appreciation;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.dto.error.ApplicationErrorResponse;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.server.BlockEngine;
import net.openhft.chronicle.decentred.server.VanillaGateway;
import town.lost.examples.appreciation.api.AppreciationGateway;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.api.AppreciationTransactions;
import town.lost.examples.appreciation.dto.*;
import town.lost.examples.appreciation.util.BalanceStore;
import town.lost.examples.appreciation.util.Balances;

/**
 * Run as a gateway before the blockchain.
 */
public class VanillaAppreciationGateway extends VanillaGateway implements AppreciationGateway {
    private final MessageRouter<AppreciationResponses> responseRouter;
    private final AppreciationTransactions blockchain;
    private final BalanceStore balanceStore;

    private final OnBalance onBalance = new OnBalance();
    private final ApplicationErrorResponse error = new ApplicationErrorResponse();

    public VanillaAppreciationGateway(
        long chainAddress,
        BlockEngine mainEngine,
        BlockEngine localEngine,
        MessageRouter<AppreciationResponses> responseRouter,
        AppreciationTransactions blockchain,
        BalanceStore balanceStore
    ) {
        super(0, chainAddress, mainEngine, localEngine);
        this.responseRouter = responseRouter;
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
        long address = queryBalance.address();
        AppreciationResponses listener = responseRouter.to(address);
        Balances balance = balanceStore.getBalances(address);
        if (balance == null) {
            error.init(queryBalance,
                    "Cannot query balance: Account doesn't exist");
            error.timestampUS(queryBalance.timestampUS());
            listener.applicationError(error);
        } else {
            onBalance.init(address, balance);
            // added for testing and overridden by the framework
            onBalance.timestampUS(queryBalance.timestampUS());
            listener.onBalance(onBalance);
        }
    }

    @Override
    public void give(Give give) {
        if (give.amount() < 0) {
            AppreciationResponses listener = responseRouter.to(give.toAddress());
            error.init(give,
                    "Cannot give a negative amount");
            error.timestampUS(give.timestampUS());
            listener.applicationError(error);
            return;
        }
        if (!validAccount(give, give.address()) || !validAccount(give, give.toAddress())) {
            return;
        }

        blockchain.give(give);
    }

    @Override
    public void topup(Topup topup) {
        if (!verifyPrivilegedServerNode(topup.address())) {
            AppreciationResponses listener = responseRouter.to(topup.address());
            error.init(topup, "Only privileged accounts can generate an auto topup");
            error.timestampUS(topup.timestampUS());
            listener.applicationError(error);
        }

        blockchain.topup(topup);
    }

    private boolean verifyPrivilegedServerNode(long address) {
        return true;
    }

    private <T extends VanillaSignedMessage<T>> boolean validAccount(T msg, long address) {
        Balances balances = balanceStore.getBalances(address);
        if (balances == null) {
            AppreciationResponses listener = responseRouter.to(address);
            error.init(msg,
                    "Cannot give balance: Account doesn't exist");
            // to set a timestamp when testing, overridden by the framework.
            error.timestampUS(msg.timestampUS());
            listener.applicationError(error);
            return false;
        }

        return true;
    }

    @Override
    public void createAddressRequest(CreateAddressRequest createAddressRequest) {
        //super.createAddressRequest(createAddressRequest);
    }
}
