package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.fundamental.base.AbstractFundamentalDtoTest;
import net.openhft.chronicle.decentred.util.KeyPair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TransactionBlockGossipEventTest {


    @Test
    void marshalUnmarshal() {
        final KeyPair kp = new KeyPair(1);

        final Bytes bytes = Bytes.allocateElasticDirect(1000);

        final TransactionBlockGossipEvent expected = new TransactionBlockGossipEvent()
            .messageType(0xFFF1)
            .protocol(17)
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            //.blockNumber(42)
            .chainAddress(43)
            ;

        expected.addressToBlockNumberMap().justPut(+1, 1);
        expected.addressToBlockNumberMap().justPut(Long.MAX_VALUE, 2);
        expected.addressToBlockNumberMap().justPut(Long.MIN_VALUE, 3);
        expected.addressToBlockNumberMap().justPut(~0, 4);
        expected.sign(kp.secretKey);

        System.out.println("expected = " + expected);
        System.out.println(expected.toHexString());

        expected.writeMarshallable(bytes);

        //bytes.readPosition(0);
        TransactionBlockGossipEvent actual = new TransactionBlockGossipEvent();
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
