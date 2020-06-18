package net.openhft.chronicle.decentred.dto.base;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

public interface TransientFieldHandler<T extends VanillaSignedMessage<T>> {

    /**
     * Resets any transient fields in the provided original message.
     *
     * @param original message
     */
    void reset(@NotNull T original);

    /***
     * Copies any transient and non-marshalled fields that from the provided original
     * to the provided target.
     *
     * @param original message (source)
     * @param target   mesaage (destination)
     *
     */
    void copyNonMarshalled(@NotNull T original, @NotNull T target);

//    /***
//     * Copies any transient fields from the provided original to the provided
//     * target whereby elements are re-created and are thus not shared between
//     * the original and target.
//     *
//     * @param original message (source)
//     * @param target   mesaage (destination)
//     *
//     */
//    void deepCopy(@NotNull T original, @NotNull T target);

//   void dereference(T target) ;

    // Wire

    /**
     * Writes any transient fields from the provided original to the provided
     * wire.
     *
     * @param original to read transient fields from
     * @param wire to write transient fields into
     */
     void writeMarshallable(@NotNull T original, @NotNull WireOut wire);

    /**
     * Reads any transient fields into the provided original from the provided
     * wire.
     *
     * @param original to write transient fields into
     * @param wire     to read transient fields from
     */
    void readMarshallable(@NotNull T original, @NotNull WireIn wire);

    // Bytes

    /**
     * Writes any transient fields from the provided original into the provided
     * bytes.
     * <p>
     * This method is used when writing internal fields into the internal
     * byte store.
     *
     * @param original to read transient fields from
     * @param bytes    to write transient fields into
     */
    void writeMarshallableInternal(@NotNull T original, @NotNull BytesOut bytes);

    /**
     * Reads any transient fields into the provided original message from the
     * provided bytes.
     *
     * @param original to write transient fields into
     * @param bytes    to read transient fields from
     */
    void readMarshallable(@NotNull T original, @NotNull BytesIn bytes);

    static <T extends VanillaSignedMessage<T>> TransientFieldHandler<T> empty() {
        return EmptyTransientFieldHandler.instance();
    }
}
