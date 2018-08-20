package net.openhft.chronicle.decentred.api;

public interface AnySystemMessage extends
        AccountManagementRequests,
        AccountManagementResponses,
        Blockchainer,
        SystemMessageListener,
        Verifier {
}
