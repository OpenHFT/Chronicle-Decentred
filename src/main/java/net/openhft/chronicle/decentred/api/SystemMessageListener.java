package net.openhft.chronicle.decentred.api;


import net.openhft.chronicle.decentred.dto.ApplicationError;
import net.openhft.chronicle.decentred.dto.CreateAccount;
import net.openhft.chronicle.decentred.dto.OnAccountCreated;
import net.openhft.chronicle.decentred.dto.Verification;

public interface SystemMessageListener {
    void createAccount(CreateAccount createAccount);

    void onAccountCreated(OnAccountCreated onAccountCreated);

    void verification(Verification verification);

    void applicatioNError(ApplicationError applicationError);
}
