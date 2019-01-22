package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.AppendableUtil;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.salt.Ed25519;

import java.net.InetAddress;
import java.net.UnknownHostException;


public enum DecentredUtil {
    ;
    public static final long MASK_32 = 0x0000_0000_FFFF_FFFFL;
    public static final int MASK_16 = 0xFFFF;
    static final long ADDRESS_MASK = 0x1FFF_FFFF_FFFF_FFFFL;
    private static final long MASK_48 = 0x0000_FFFF_FFFF_FFFFL;
    private static final int MASK_8 = 0xFF;


    public static long parseAddress(CharSequence text) {
        switch (count(text, ':')) {
            case 0:
                return parseBase32(text);
            case 1:
                return parseIpPort(text.toString());
            case 2:
                return parseIpPortKey(text.toString());
            default:
                throw new IllegalArgumentException("Not an address format '" + text + "'");
        }
    }

    private static int count(CharSequence cs, char ch) {
        int n = 0;
        for (int i = 0; i < cs.length(); i++)
            if (ch == cs.charAt(i))
                n++;
        return n;
    }

    private static long parseBase32(CharSequence cs) {
        return LetterBase32.decode(cs) | ~ADDRESS_MASK;
    }

    private static long parseIpPortKey(String text) {
        int last = text.lastIndexOf(':');
        return (parseIpPort(text.substring(0, last)) << 16) + Integer.parseInt(text.substring(last + 1), 16);
    }

    private static long parseIpPort(String text) {
        try {
            int last = text.lastIndexOf(':');
            InetAddress address = InetAddress.getByName(text.substring(0, last));
            int port = Integer.parseInt(text.substring(last + 1));
            return ((address.hashCode() & MASK_32) << 16) + port;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Not an address format '" + text + "'", e);
        }
    }


    public static long toAddress(BytesStore publicKey) {
        return publicKey.readLong(publicKey.readLimit() - Long.BYTES);
    }

    public static BytesStore testPrivateKey(long seed) {
        Bytes privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.zeroOut(0, Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.writeSkip(Ed25519.PRIVATE_KEY_LENGTH - Long.BYTES);
        privateKey.writeLong(seed);
        return privateKey;
    }

    public static String toAddressString(long address) {
        StringBuilder sb = new StringBuilder(13);
        appendAddress(sb, address);
        return sb.toString();
    }

    public static void appendAddress(StringBuilder text, long value) {
        // plain address
        if ((value | MASK_48) == MASK_48)
            ipPort(text, value);
        else if (isAddressNamed(value))
            base32(text, value & ADDRESS_MASK);
        else
            ipPortKey(text, value);
    }

    public static boolean isAddressNamed(long value) {
        return (value >>> 60) >= ~ADDRESS_MASK >>> 60;
    }

    private static void base32(StringBuilder text, long value) {
        LetterBase32.encode(text, value & ADDRESS_MASK);
    }

    private static void ipPortKey(StringBuilder text, long value) {
        ipPort(text, value >>> 16);
        text.append(':');
        text.append(Integer.toHexString((int) (value & MASK_16)));
    }

    private static void ipPort(StringBuilder text, long value) {
        AppendableUtil.append(text, (value >> 40) & MASK_8);
        text.append('.');
        AppendableUtil.append(text, (value >> 32) & MASK_8);
        text.append('.');
        AppendableUtil.append(text, (value >> 24) & MASK_8);
        text.append('.');
        AppendableUtil.append(text, (value >> 16) & MASK_8);
        text.append(':');
        AppendableUtil.append(text, value & MASK_16);
    }

    public static int parseRegion(String region) {
        return (int) (LetterBase32.decode(region) & MASK_16);
    }
}
