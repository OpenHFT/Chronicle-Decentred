package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.wire.LongConverter;

public class AddressConverter implements LongConverter {
    @Override
    public long parse(CharSequence text) {
        return DecentredUtil.parseAddress(text);
    }


    @Override
    public void append(StringBuilder text, long value) {
        DecentredUtil.appendAddress(text, value);
    }
}
