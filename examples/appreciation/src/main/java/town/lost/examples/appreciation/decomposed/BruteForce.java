package town.lost.examples.appreciation.decomposed;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.salt.Ed25519;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class BruteForce {

    public static final int THREADS = 8;
    public static final long STEP = Long.MAX_VALUE / THREADS;
    public static final long OFFSET = (10_000 * 3_600 * 10 ) * 3;

    public static final long TARGET = (0L << 56) + (0L << 48) + (0L << 40) + (0L << 32);
    public static final long TARGET_MASK = 0xEFFFFFFF00000000l;

    public static final void main(String[] argv) {

List<Thread> threadList = LongStream.rangeClosed(0, THREADS)
            .map(i -> i * STEP + OFFSET)
            .mapToObj(i -> new Thread(() -> eval(i), "from " + i))
            .collect(Collectors.toList())
            ;

        System.out.println(
            threadList.stream()
            .map(Thread::getName)
            .collect(Collectors.joining(", "))
        );

        threadList.forEach(Thread::start);

    }

private static void eval(final long from) {
        final long start = System.currentTimeMillis();
        boolean printed = false;

        final Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        final Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        final Bytes privateKey = Bytes.allocateDirect(Ed25519.PRIVATE_KEY_LENGTH);
        privateKey.zeroOut(0, Ed25519.PRIVATE_KEY_LENGTH);
        final StringBuilder  sb = new StringBuilder();

        privateKey.writeSkip(Ed25519.PRIVATE_KEY_LENGTH - (long) Long.BYTES);
        privateKey.writeLong(0); // First dummy

        for (long seed = from; seed < Long.MAX_VALUE; seed++) {
            publicKey.readPosition(0);
            publicKey.writePosition(0);

            secretKey.readPosition(0);
            secretKey.writePosition(0);

            privateKey.writeLong(Ed25519.PRIVATE_KEY_LENGTH - (long) Long.BYTES, seed);
            privateKey.readPosition(0);

            Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);

            final long address = DecentredUtil.toAddress(publicKey);
            if ((address & 0xE000_0000_0000_0000L) != 0xE000_0000_0000_0000L) {
                if ((address & TARGET_MASK) == TARGET) {
                    sb.setLength(0);
                    DecentredUtil.appendAddress(sb, address);
                    final String addressString = sb.toString();
                    if (addressString.startsWith("0.0.0.0")) {
                        System.out.println("Found seed " + seed + " " + addressString);
                    }
                }
            }

            if (seed % 1_000_000 == 0) {
                final long elapsed = System.currentTimeMillis() - start;
                final long evaluated = seed - from;
                if (!printed && evaluated > 500_000) {
                    printed =  true;
                    final double tps = 1000.0 * (double) evaluated / elapsed;
                    System.out.println(String.format("%,d @ %,.0f ops/s avg after %d s", evaluated, tps, elapsed/1000));
                }
            }
 }
    }

}
