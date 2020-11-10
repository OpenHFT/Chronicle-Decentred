package net.openhft.chronicle.decentred.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AddressLongConverterTest {

    @Test
    void parseAppend() {
        AddressLongConverter ac = new AddressLongConverter();
        StringBuilder sb = new StringBuilder();
        for (String s : ("abcdefghiklm," +
                "ol234s67bq," +
                "peter.lawrey," +
                "abcdefghik2").split(",")) {
            long l = ac.parse(s);
            sb.setLength(0);
            ac.append(sb, l);
            assertEquals(s, sb.toString());
        }
    }

}