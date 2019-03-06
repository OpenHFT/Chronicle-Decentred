package net.openhft.chronicle.decentred.dto.chainevent;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.dto.base.TransientFieldHandler;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.dto.base.trait.HasAddressToBlockNumberMap;
import net.openhft.chronicle.decentred.dto.base.trait.HasChainAddress;
import net.openhft.chronicle.decentred.internal.unmodifiable.UnmodifiableLongLongMap;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.LongLongMap;
import net.openhft.chronicle.decentred.util.LongU32Writer;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.openhft.chronicle.decentred.dto.chainevent.AddressToBlockNumberUtil.ADDRESS_TO_BLOCK_NUMBER_MAP_NAME;

/**
 * An EndOfRoundBlockEvent is a <em>chain event</em> that notifies which block numbers are in the next round.
 * <p>
 * Pointers for each address are included in this message with an association between an address and
 * the block number that particular address is currently at. Basically, these block numbers are like
 * a cursor which points to transactions.
 * <p>
 * The cursors are monotonic pointers, usually in the order 0, 1, 2, ...
 */
// Add validation
public final class EndOfRoundBlockEvent extends VanillaSignedMessage<EndOfRoundBlockEvent> implements
        HasChainAddress<EndOfRoundBlockEvent>,
        HasAddressToBlockNumberMap<EndOfRoundBlockEvent> {

    @LongConversion(AddressLongConverter.class)
    private long chainAddress;
    private transient LongLongMap addressToBlockNumberMap;


    @Override
    public EndOfRoundBlockEvent chainAddress(long chainAddress) {
        assertNotSigned();
        this.chainAddress = chainAddress;
        return this;
    }

    @Override
    public LongLongMap addressToBlockNumberMap() {
        if (signed()) {
            if (addressToBlockNumberMap == null) {
                return new UnmodifiableLongLongMap(LongLongMap.withExpectedSize(0));
            }
            return new UnmodifiableLongLongMap(addressToBlockNumberMap);
        } else {
            if (addressToBlockNumberMap == null) {
                addressToBlockNumberMap = LongLongMap.withExpectedSize(16);
            }
            return addressToBlockNumberMap;
        }
    }

    @Override
    public long chainAddress() {
        return chainAddress;
    }


    // Handling of transient fields

    private static final TransientFieldHandler<EndOfRoundBlockEvent> TRANSIENT_FIELD_HANDLER = new CustomTransientFieldHandler();

    @Override
    public TransientFieldHandler<EndOfRoundBlockEvent> transientFieldHandler() {
        return TRANSIENT_FIELD_HANDLER;
    }

    private static final class CustomTransientFieldHandler implements TransientFieldHandler<EndOfRoundBlockEvent> {

        @Override
        public void reset(EndOfRoundBlockEvent original) {
            original.addressToBlockNumberMap = null;
        }

        @Override
        public void copyNonMarshalled(@NotNull EndOfRoundBlockEvent original, @NotNull EndOfRoundBlockEvent target) {
            // All transient fields are marshalled
        }

/*        @Override
        public void copyNonMarshalled(@NotNull EndOfRoundBlockEvent original, @NotNull EndOfRoundBlockEvent target) {
            AddressToBlockNumberUtil.copyNonMarshalled(original.addressToBlockNumberMap, m -> target.addressToBlockNumberMap = m);
        }

        @Override
        public void deepCopy(@NotNull EndOfRoundBlockEvent original, @NotNull EndOfRoundBlockEvent target) {
            copyNonMarshalled(original, target);
        }*/

        @Override
        public void writeMarshallable(@NotNull EndOfRoundBlockEvent original, @NotNull WireOut wire) {
            AddressToBlockNumberUtil.writeMap(wire, ADDRESS_TO_BLOCK_NUMBER_MAP_NAME, original.addressToBlockNumberMap);
        }

        @Override
        public void readMarshallable(EndOfRoundBlockEvent original, @NotNull WireIn wire) {
            AddressToBlockNumberUtil.readMap(wire, ADDRESS_TO_BLOCK_NUMBER_MAP_NAME, original.addressToBlockNumberMap());
        }

        @Override
        public void writeMarshallableInternal(EndOfRoundBlockEvent original, @NotNull BytesOut bytes) {
            AddressToBlockNumberUtil.writeMap(bytes, ADDRESS_TO_BLOCK_NUMBER_MAP_NAME, original.addressToBlockNumberMap);
        }

        @Override
        public void readMarshallable(@NotNull EndOfRoundBlockEvent original, @NotNull BytesIn bytes) {
            AddressToBlockNumberUtil.readMap(bytes, ADDRESS_TO_BLOCK_NUMBER_MAP_NAME, m -> original.addressToBlockNumberMap = m);
        }
    }


}

