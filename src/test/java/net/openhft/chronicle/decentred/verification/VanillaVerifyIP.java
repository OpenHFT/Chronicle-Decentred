package net.openhft.chronicle.decentred.verification;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.Verifier;
import net.openhft.chronicle.decentred.dto.Invalidation;
import net.openhft.chronicle.decentred.dto.Verification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.openhft.chronicle.decentred.api.MessageRouter.DEFAULT_CONNECTION;

public class VanillaVerifyIP implements Verifier {
    private final MessageRouter<Verifier> client;
    private final Map<BytesStore, List<Verification>> verifyMap = new HashMap<>();

    public VanillaVerifyIP(MessageRouter<Verifier> client) {
        this.client = client;
    }

    @Override
    public void onConnection() {
        Verifier to = client.to(DEFAULT_CONNECTION);
        for (List<Verification> verificationList : verifyMap.values()) {
            for (Verification verification : verificationList) {
                to.verification(verification);
            }
        }
    }

    @Override
    public void verification(Verification verification) {
        List<Verification> verificationList = verifyMap.computeIfAbsent(verification.keyVerified(), k -> new ArrayList<>());
        // TODO check it is not already in the list.
        Verification v2 = verification.deepCopy();
        verificationList.add(v2);
//        client.to(DEFAULT_CONNECTION)
//                .verification(verification);
    }

    @Override
    public void invalidation(Invalidation record) {
        verifyMap.remove(record.publicKey());
    }
}
