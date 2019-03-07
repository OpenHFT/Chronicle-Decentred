package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.blockevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;
import net.openhft.chronicle.decentred.server.BlockReplayer;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.TextMethodTester;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


final class EndOfRoundBlockEventTest {
    static void test(String basename) {
        TextMethodTester<BlockReplayer> tester = new TextMethodTester<>(
                basename + "/in.yaml",
                EndOfRoundBlockEventTest::createGateway,
                BlockReplayer.class,
                basename + "/out.yaml");
        tester.setup(basename + "/setup.yaml");
        try {
            tester.run();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        assertEquals(tester.expected(), tester.actual());
    }

    private static BlockReplayer createGateway(BlockReplayer tester) {
        return new BlockReplayer() {
            @Override
            public void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent) {
                tester.transactionBlockEvent(transactionBlockEvent);
            }

            @Override
            public void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent) {
                tester.endOfRoundBlockEvent(endOfRoundBlockEvent);
            }

            @Override
            public void replayBlocks() {
                tester.replayBlocks();
            }
        };
    }

    @Test
    void testVerifyOne() {
        test("raw/endOfRoundBlockEvent");
    }

    @Test
    void test() {
        final KeyPair kp = new KeyPair(1);

        final Bytes bytes = Bytes.allocateElasticDirect(1000);

        final EndOfRoundBlockEvent expected = new EndOfRoundBlockEvent()
            .messageType(0xFFF3)
            .protocol(17)
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .chainAddress(43)
            ;

        expected.addressToBlockNumberMap().justPut(+1, 1);
        expected.addressToBlockNumberMap().justPut(Long.MAX_VALUE, 2);
        expected.addressToBlockNumberMap().justPut(Long.MIN_VALUE, 3);
        expected.addressToBlockNumberMap().justPut(~0, 4);
        expected.sign(kp.secretKey);
        System.out.println(expected);
/*        System.out.println("expected = " + expected);
        System.out.println(expected.toHexString());*/

        expected.writeMarshallable(bytes);

        //bytes.readPosition(0);
        EndOfRoundBlockEvent actual = new EndOfRoundBlockEvent();
        actual.readMarshallable(bytes);

        final String actualString = actual.toString();

        String expectedString = expected.toString();
        assertEquals(expectedString, actualString);

        AbstractFundamentalDtoTest.assertContains(actualString, "  addressToBlockNumberMap: {\n" +
                "    a: 1,\n" +
                "    g777777777777: 2,\n" +
                "    h............: 3,\n" +
                "    o777777777777: 4\n" +
                "  }");

    }
}
