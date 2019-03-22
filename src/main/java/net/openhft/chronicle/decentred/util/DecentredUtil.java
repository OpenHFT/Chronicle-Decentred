package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.BytesStore;


public enum DecentredUtil {
    ;
    public static final long MASK_32 = 0x0000_0000_FFFF_FFFFL;
    public static final int MASK_16 = 0xFFFF;
    private static final int MASK_8 = 0xFF;


    public static long parseAddress(CharSequence text) {
        return LetterBase32.decode(text);
    }

    public static long toAddress(BytesStore publicKey) {
        return publicKey.readLong(publicKey.readLimit() - Long.BYTES);
    }

    public static String toAddressString(long address) {
        StringBuilder sb = new StringBuilder(13);
        appendAddress(sb, address);
        return sb.toString();
    }

    public static void appendAddress(StringBuilder text, long value) {
        base32(text, value);
    }

    private static void base32(StringBuilder text, long value) {
        LetterBase32.encode(text, value);
    }
}
