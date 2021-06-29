package net.openhft.chronicle.decentred.internal.unmodifiable;

enum ThrowUtil {
    ; // none

    static UnsupportedOperationException newUnsupportedOperationException() {
        return new UnsupportedOperationException("This Container is unmodifiable.");
    }
}
