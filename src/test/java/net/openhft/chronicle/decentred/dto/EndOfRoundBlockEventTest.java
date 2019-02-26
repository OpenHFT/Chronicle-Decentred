package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.chainevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.TextMethodTester;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EndOfRoundBlockEventTest {
    static void test(String basename) {
        TextMethodTester<EndOfRoundBlockEventTester> tester = new TextMethodTester<>(
                basename + "/in.yaml",
                EndOfRoundBlockEventTest::createGateway,
            EndOfRoundBlockEventTester.class,
                basename + "/out.yaml");
        tester.setup(basename + "/setup.yaml");
        try {
            tester.run();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        assertEquals(tester.expected(), tester.actual());
    }

    private static EndOfRoundBlockEvent createGateway(EndOfRoundBlockEventTester tester) {
        return new EndOfRoundBlockEvent();
    }

    @Test
    public void testVerifyOne() {
        test("raw/endOfRoundBlockEvent");
    }

    @Test
    public void test() {
        final KeyPair kp = new KeyPair(1);

        final Bytes bytes = Bytes.allocateElasticDirect(1000);

        final EndOfRoundBlockEvent expected = new EndOfRoundBlockEvent()
            .weekNumber(1)
            .messageType(0xFFF3)
            .protocol(17)
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .blockNumber(42)
            .chainAddress(43)
            ;

        expected.addressToBlockNumberMap().justPut(0, 16);
        expected.addressToBlockNumberMap().justPut((192L << 56) + (168L << 48) + (1L << 40) + (147L << 32)+ (10000L << 16), 17); // 192.168.1.147:10000
        expected.sign(kp.secretKey);

/*        System.out.println("expected = " + expected);
        System.out.println(expected.toHexString());*/

        expected.writeMarshallable(bytes);

        //bytes.readPosition(0);
        EndOfRoundBlockEvent actual = new EndOfRoundBlockEvent();
        actual.readMarshallable(bytes);

        final String expectedString = actual.toString();

        int length = expectedString.indexOf("addressToBlockNumberMap: {");

        assertEquals(expected.toString().substring(0, length), actual.toString().substring(0, length));

        assertTrue(actual.toString().contains("\"192.168.1.147:10000\": 17"));
        assertTrue(actual.toString().contains("\"0.0.0.0:0\": 16"));

    }


}
