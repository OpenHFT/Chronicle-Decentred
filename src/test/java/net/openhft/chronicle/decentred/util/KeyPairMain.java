package net.openhft.chronicle.decentred.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class KeyPairMain {
    public static void main(String[] args) {
        AtomicInteger count = new AtomicInteger();
        IntStream.range(0, 128)
                .parallel()
                .forEach(i -> {
                    KeyPair kp = new KeyPair((char) i);
                    long address = DecentredUtil.toAddress(kp.publicKey);
                    if (true) {
                        String s = DecentredUtil.toAddressString(address);
                        int dots = s.split("[.]").length;
                        if (dots > 0)
                            System.out.println((char) i + ": " + s);
                    }
                    int c = count.incrementAndGet();
                    if (c % 1_000_000 == 0)
                        System.out.println(c);
                    kp.publicKey.release();
                    kp.secretKey.release();
                });

    }
}
