package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.wire.IntConverter;

import java.time.ZoneOffset;

public class OffsetIntConverter implements IntConverter {
    @Override
    public int parse(CharSequence text) {
        ZoneOffset zo = ZoneOffset.of(text.toString());
        return zo.getTotalSeconds();
    }

    @Override
    public void append(StringBuilder text, int value) {
        text.append(ZoneOffset.ofTotalSeconds(value));
    }
}
