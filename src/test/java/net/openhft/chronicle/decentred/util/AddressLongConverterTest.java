package net.openhft.chronicle.decentred.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddressLongConverterTest {
    @Test
    public void parseAppend() {
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