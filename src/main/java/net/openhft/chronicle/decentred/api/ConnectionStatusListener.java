package net.openhft.chronicle.decentred.api;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.remote.net.TCPConnection;

/**
 * The framework letting the gateway know a connection has appeared of disappeared.
 */
public interface    ConnectionStatusListener {
    /**
     * Called by the framework on a new connection
     *
     * @param connection connected
     */
    default void onConnection(TCPConnection connection) {
        Jvm.debug().on(getClass(), "Connected " + connection);
    }

    /**
     * Called by the framework on disconnection
     *
     * @param connection disconnected
     */
    default void onDisconnection(TCPConnection connection) {
        Jvm.debug().on(getClass(), "Disconnected " + connection);
    }

}
