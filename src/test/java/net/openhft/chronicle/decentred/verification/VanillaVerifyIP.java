package net.openhft.chronicle.decentred.verification;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.Verifier;
import net.openhft.chronicle.decentred.dto.InvalidationCommand;
import net.openhft.chronicle.decentred.dto.VerificationEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.openhft.chronicle.decentred.api.MessageRouter.DEFAULT_CONNECTION;

public class VanillaVerifyIP implements Verifier {
    private final MessageRouter<Verifier> client;
    private final Map<BytesStore, List<VerificationEvent>> verifyMap = new HashMap<>();

    public VanillaVerifyIP(MessageRouter<Verifier> client) {
        this.client = client;
    }

    @Override
    public void onConnection() {
        Verifier to = client.to(DEFAULT_CONNECTION);
        for (List<VerificationEvent> verificationEventList : verifyMap.values()) {
            for (VerificationEvent verificationEvent : verificationEventList) {
                to.verification(verificationEvent);
            }
        }
    }

    @Override
    public void verification(VerificationEvent verificationEvent) {
        List<VerificationEvent> verificationEventList = verifyMap.computeIfAbsent(verificationEvent.keyVerified(), k -> new ArrayList<>());
        // TODO check it is not already in the list.
        VerificationEvent v2 = verificationEvent.deepCopy();
        verificationEventList.add(v2);
        client.to(DEFAULT_CONNECTION)
                .verification(verificationEvent);
    }

    @Override
    public void invalidation(InvalidationCommand record) {
        verifyMap.remove(record.publicKey());
    }
}
