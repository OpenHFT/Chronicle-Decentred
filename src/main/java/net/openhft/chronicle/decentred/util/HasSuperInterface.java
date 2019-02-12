package net.openhft.chronicle.decentred.util;

public interface HasSuperInterface<T> {

    /**
     * Returns the super interface that is common for all messages
     * to be handled by this object.
     *
     * @return the super interface that is common for all messages
     * to be handled by this object
     */
    Class<T> superInterface();
}
