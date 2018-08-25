package net.openhft.chronicle.decentred.verification;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.api.ConnectionStatusListener;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.Verifier;
import net.openhft.chronicle.decentred.dto.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.remote.net.TCPConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.openhft.chronicle.decentred.api.MessageRouter.DEFAULT_CONNECTION;

public class VanillaVerifyIP implements Verifier, ConnectionStatusListener {
    private final MessageRouter<Verifier> client;
    private final Map<BytesStore, List<VerificationEvent>> verifyMap = new HashMap<>();

    public VanillaVerifyIP(MessageRouter<Verifier> client) {
        this.client = client;
    }

    @Override
    public void onConnection(TCPConnection connection) {
        onConnection();
    }

    public void onConnection() {
        Verifier to = client.to(DEFAULT_CONNECTION);
        for (List<VerificationEvent> verificationEventList : verifyMap.values()) {
            for (VerificationEvent verificationEvent : verificationEventList) {
                to.verificationEvent(verificationEvent);
            }
        }
    }

    @Override
    public void verificationEvent(VerificationEvent verificationEvent) {
        List<VerificationEvent> verificationEventList = verifyMap.computeIfAbsent(verificationEvent.keyVerified(), k -> new ArrayList<>());
        // TODO check it is not already in the list.
        VerificationEvent v2 = verificationEvent.deepCopy();
        verificationEventList.add(v2);
        client.to(DEFAULT_CONNECTION)
                .verificationEvent(verificationEvent);
    }

    @Override
    public void invalidationEvent(InvalidationEvent record) {
        verifyMap.remove(record.publicKey());
    }
}
