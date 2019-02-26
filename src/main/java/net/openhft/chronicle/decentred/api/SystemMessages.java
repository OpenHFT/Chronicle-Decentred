package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateChainRequest;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateTokenRequest;

public interface SystemMessages extends
        WeeklyEvents,
        AddressManagementRequests,
        SystemMessageListener {
    @MethodId(0x0101)
    void createChainRequest(CreateChainRequest createChainRequest);

    @MethodId(0x0102)
    void createTokenRequest(CreateTokenRequest createTokenRequest);
}
