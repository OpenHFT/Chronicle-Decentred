package net.openhft.chronicle.decentred.api;

public interface SystemMessages extends
        WeeklyEvents,
        AccountManagementRequests,
        SystemMessageListener,
        MessageListener {
}
