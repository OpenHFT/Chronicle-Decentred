package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.AppendableUtil;
import net.openhft.chronicle.wire.LongConverter;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static net.openhft.chronicle.decentred.util.DtoRegistry.MASK_16;

public class AddressConverter implements LongConverter {

    private static final long MASK_48 = 0x0000_FFFF_FFFF_FFFFL;
    private static final long MASK_32 = 0x0000_0000_FFFF_FFFFL;
    private static final long ACCOUNT_MASK = 0x1FFF_FFFF_FFFF_FFFFL;
    private static final int MASK_8 = 0xFF;

    private static int count(CharSequence cs, char ch) {
        int n = 0;
        for (int i = 0; i < cs.length(); i++)
            if (ch == cs.charAt(i))
                n++;
        return n;
    }

    @Override
    public long parse(CharSequence text) {
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

    private long parseBase32(CharSequence cs) {
        return LetterBase32.decode(cs) | ~ACCOUNT_MASK;
    }

    private long parseIpPortKey(String text) {
        int last = text.lastIndexOf(':');
        return (parseIpPort(text.substring(0, last)) << 16) + Integer.parseInt(text.substring(last + 1), 16);
    }

    private long parseIpPort(String text) {
        try {
            int last = text.lastIndexOf(':');
            InetAddress address = InetAddress.getByName(text.substring(0, last));
            int port = Integer.parseInt(text.substring(last + 1));
            return ((address.hashCode() & MASK_32) << 16) + port;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Not an address format '" + text + "'", e);
        }
    }

    @Override
    public void append(StringBuilder text, long value) {
        // plain address
        if ((value | MASK_48) == MASK_48)
            ipPort(text, value);
        else if ((value >>> 60) < 0xE)
            ipPortKey(text, value);
        else base32(text, value & ACCOUNT_MASK);
    }

    private void base32(StringBuilder text, long value) {
        LetterBase32.encode(text, value & ACCOUNT_MASK);
    }

    private void ipPortKey(StringBuilder text, long value) {
        ipPort(text, value >>> 16);
        text.append(':');
        text.append(Integer.toHexString((int) (value & MASK_16)));
    }

    private void ipPort(StringBuilder text, long value) {
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
}
