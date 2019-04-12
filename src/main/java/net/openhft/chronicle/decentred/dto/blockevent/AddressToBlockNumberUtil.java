package net.openhft.chronicle.decentred.dto.blockevent;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.LongLongMap;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Consumer;

enum AddressToBlockNumberUtil {;

    public static final String ADDRESS_TO_BLOCK_NUMBER_MAP_NAME = "addressToBlockNumberMap";

    static void copy(LongLongMap source, @NotNull Consumer<LongLongMap> setter) {
        if (source == null) {
            setter.accept(null);
        } else {
            final LongLongMap map = LongLongMap.withExpectedSize(source.size());
            map.putAll(source);
            setter.accept(map);
        }
    }

    static void writeMap(@NotNull WireOut wire, @NotNull String name, LongLongMap map) {
        if (map == null || map.size() == 0) {
            return;
        }
        wire.write(name).marshallable(out -> {
            long[] keys = map.keySet().toLongArray();
            for (int i = 0; i < keys.length; i++) keys[i] -= Long.MIN_VALUE;
            Arrays.sort(keys);
            for (int i = 0; i < keys.length; i++) keys[i] += Long.MIN_VALUE;
            for (long key : keys) {
                out.write(DecentredUtil.toAddressString(key)).int64(map.get(key));
            }
        });
    }

    static void readMap(@NotNull WireIn wire, @NotNull String name, @NotNull LongLongMap map) {
        map.clear();
        wire.read(name).marshallable(in -> {
            while (in.hasMore()) {
                final String key = in.readEvent(String.class);
                if (key == null || key.length() == 0)
                    return;
                final long addr = DecentredUtil.parseAddress(key);
                final long value = in.getValueIn().int64();
                map.justPut(addr, value);
            }
        });
    }

    static void writeMap(@NotNull BytesOut bytes, @NotNull String name, LongLongMap map) {
        if (map == null || map.size() == 0) {
            bytes.writeStopBit(0);
        } else {
            bytes.writeStopBit(map.size());
            map.forEach((address, blockNumber) -> bytes.writeLong(address).writeLong(blockNumber));
        }
    }

    static void readMap(@NotNull BytesIn bytes, @NotNull String name, @NotNull Consumer<LongLongMap> setter) {
        final int entries = (int) bytes.readStopBit();
        final LongLongMap map = LongLongMap.withExpectedSize(entries);
        for (int i = 0; i < entries; i++) {
            map.justPut(bytes.readLong(), bytes.readLong());
        }
        setter.accept(map);
    }


}
