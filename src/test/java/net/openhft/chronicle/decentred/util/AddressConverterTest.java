package net.openhft.chronicle.decentred.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddressConverterTest {
    @Test
    public void parseAppend() {
        AddressConverter ac = new AddressConverter();
        StringBuilder sb = new StringBuilder();
        for (String s : ("1.1.1.1:1111," +
                "2.2.2.2:2222:deaf," +
                "abcdefghiklm," +
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