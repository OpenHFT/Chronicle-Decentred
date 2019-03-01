package net.openhft.chronicle.decentred.dto.chainevent;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.decentred.dto.base.TransientFieldHandler;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.dto.base.trait.HasAddressToBlockNumberMap;
import net.openhft.chronicle.decentred.dto.base.trait.HasChainAddress;
import net.openhft.chronicle.decentred.util.AddressLongConverter;
import net.openhft.chronicle.decentred.util.LongLongMap;
import net.openhft.chronicle.wire.LongConversion;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;


import static net.openhft.chronicle.decentred.dto.chainevent.AddressToBlockNumberUtil.ADDRESS_TO_BLOCK_NUMBER_MAP_NAME;

// Block number is N:th round in a week for the round.
/**
 * An TransactionBlockGossipEvent is a <em>chain event</em> that ...
 *
 */
public final class TransactionBlockGossipEvent extends VanillaSignedMessage<TransactionBlockGossipEvent> implements
    HasChainAddress<TransactionBlockGossipEvent>,
    HasAddressToBlockNumberMap<TransactionBlockGossipEvent>
{
    @LongConversion(AddressLongConverter.class)
    private long chainAddress;
    private transient LongLongMap addressToBlockNumberMap;

    @Override
    public long chainAddress() {
        return chainAddress;
    }

    @Override
    public TransactionBlockGossipEvent chainAddress(long chainAddress) {
        assertNotSigned();
        this.chainAddress = chainAddress;
        return this;
    }

    @Override
    public LongLongMap addressToBlockNumberMap() {
        if (addressToBlockNumberMap == null) {
            addressToBlockNumberMap = LongLongMap.withExpectedSize(16);
        }
        return addressToBlockNumberMap;
    }

    // Handling of transient fields

    private static final TransientFieldHandler<TransactionBlockGossipEvent> TRANSIENT_FIELD_HANDLER = new CustomTransientFieldHandler();

    @Override
    public TransientFieldHandler<TransactionBlockGossipEvent> transientFieldHandler() {
        return TRANSIENT_FIELD_HANDLER;
    }

    private static final class CustomTransientFieldHandler implements TransientFieldHandler<TransactionBlockGossipEvent> {

        @Override
        public void reset(TransactionBlockGossipEvent original) {
            original.addressToBlockNumberMap = null;
        }

        @Override
        public void copy(@NotNull TransactionBlockGossipEvent original, @NotNull TransactionBlockGossipEvent target) {
            AddressToBlockNumberUtil.copy(original.addressToBlockNumberMap, m -> target.addressToBlockNumberMap = m);
        }

        @Override
        public void deepCopy(@NotNull TransactionBlockGossipEvent original, @NotNull TransactionBlockGossipEvent target) {
            copy(original, target);
        }

        @Override
        public void writeMarshallable(@NotNull TransactionBlockGossipEvent original, @NotNull WireOut wire) {
            AddressToBlockNumberUtil.writeMap(wire, ADDRESS_TO_BLOCK_NUMBER_MAP_NAME, original.addressToBlockNumberMap);
        }

        @Override
        public void readMarshallable(TransactionBlockGossipEvent original, WireIn wire) {
            AddressToBlockNumberUtil.readMap(wire, ADDRESS_TO_BLOCK_NUMBER_MAP_NAME, original.addressToBlockNumberMap());
        }

        @Override
        public void writeMarshallableInternal(TransactionBlockGossipEvent original, BytesOut bytes) {
            AddressToBlockNumberUtil.writeMap(bytes, ADDRESS_TO_BLOCK_NUMBER_MAP_NAME, original.addressToBlockNumberMap);
        }

        @Override
        public void readMarshallable(@NotNull TransactionBlockGossipEvent original, @NotNull BytesIn bytes) {
            AddressToBlockNumberUtil.readMap(bytes, ADDRESS_TO_BLOCK_NUMBER_MAP_NAME, m -> original.addressToBlockNumberMap = m);
        }
    }


}
