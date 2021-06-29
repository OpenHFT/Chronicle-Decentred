package net.openhft.chronicle.decentred.util;

import java.util.Arrays;

public enum LetterBase32 {
    ; // none

    private static final int MASK_5 = 0x1F;
    private static final ThreadLocal<StringBuilder> SB_TL = ThreadLocal.withInitial(StringBuilder::new);
    private static final byte[] VALUES = new byte[128];
    private static final char[] ENCODE = ".abcdefghijklmnopqrstuvwxyz23467".toCharArray();

    static {
        Arrays.fill(VALUES, (byte) -1);
        for (int i = 0; i < ENCODE.length; i++) {
            VALUES[ENCODE[i]] = (byte) i;
            VALUES[Character.toUpperCase(ENCODE[i])] = (byte) i;
        }
        VALUES['@'] = 0;
        VALUES['0'] = VALUES['o'];
        VALUES['1'] = VALUES['l'];
        VALUES['5'] = VALUES['S'];
        VALUES['8'] = VALUES['B'];
        VALUES['9'] = VALUES['q'];
    }

    public static long decode(CharSequence text) {
        long value = 0;
        for (int i = 0; i < text.length(); i++) {
            byte code = VALUES[text.charAt(i)];
            if (code < 0)
                throw new IllegalArgumentException("Cannot decode " + text);
            value = (value << 5) + (code & 0xff);
        }
        return value;
    }

    public static String encode(long value) {
        StringBuilder sb = SB_TL.get();
        sb.setLength(0);
        encode(sb, value);
        return sb.toString();
    }

    public static void encode(StringBuilder sb, long value) {
        int start = sb.length();
        do {
            sb.append(ENCODE[(int) (value & MASK_5)]);
            value >>>= 5;
        } while (value != 0);
        int end = sb.length() - 1;
        while (start < end) {
            char t = sb.charAt(start);
            sb.setCharAt(start, sb.charAt(end));
            sb.setCharAt(end, t);
            start++;
            end--;
        }
    }
}
