package net.openhft.chronicle.decentred.api;

public interface SystemMessages extends
    BlockEvents,
    AddressManagementRequests,
    ChainLifecycleRequests,
    SystemMessageListener {}
