package net.openhft.chronicle.decentred.verification;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.server.VanillaVerifyIP;
import net.openhft.chronicle.salt.Ed25519;
import net.openhft.chronicle.wire.TextMethodTester;
import net.openhft.chronicle.wire.TextWire;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class VerifyIPTest {
    static void test(String basename) {
        TextMethodTester<VerifyIPTester> tester = new TextMethodTester<>(
                basename + "/in.yaml",
                VerifyIPTest::createGateway,
                VerifyIPTester.class,
                basename + "/out.yaml");
        tester.setup(basename + "/setup.yaml");
        try {
            tester.run();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        assertEquals(tester.expected(), tester.actual());
    }

    private static VanillaVerifyIP createGateway(VerifyIPTester tester) {
        return new VanillaVerifyIP(tester);
    }

    public static void main(String[] args) {
        Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.generatePublicAndSecretKey(publicKey, secretKey);

        Bytes publicKey2 = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        Bytes secretKey2 = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.generatePublicAndSecretKey(publicKey2, secretKey2);

        TextWire wire = new TextWire(Bytes.elasticHeapByteBuffer(128));
        VerifyIPTester tester = wire.methodWriter(VerifyIPTester.class);
        tester.onConnection(null);
        VerificationEvent verificationEvent = new VerificationEvent()
                .protocol(1).messageType(4)
                .keyVerified(publicKey)
                .address(publicKey.readLong(0))
                .timestampUS(SystemTimeProvider.INSTANCE.currentTimeMicros())
                .publicKey(publicKey)
                .keyVerified(publicKey2);
        tester.verificationEvent(verificationEvent);

        System.out.println(wire);

    }

    @Test
    public void testVerifyOne() {
        test("verification/one");
    }
}
