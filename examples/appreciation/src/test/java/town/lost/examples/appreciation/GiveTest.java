package town.lost.examples.appreciation;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.TextWire;
import org.junit.jupiter.api.Test;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OpeningBalance;

import static town.lost.examples.appreciation.TestUtils.test;

final class GiveTest {
    public static void main(String[] args) {
        KeyPair kp7 = new KeyPair(7);

        KeyPair kp17 = new KeyPair(17);

        TextWire wire = new TextWire(Bytes.elasticHeapByteBuffer(128));
        AppreciationTester tester = wire.methodWriter(AppreciationTester.class);
        tester.createAddressRequest(new CreateAddressRequest()
                .address(0)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .publicKey(kp7.publicKey));
        tester.createAddressRequest(new CreateAddressRequest()
                .address(0)
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .publicKey(kp17.publicKey));

        long address1 = DecentredUtil.toAddress(kp7.publicKey);
        long address2 = DecentredUtil.toAddress(kp17.publicKey);
        tester.openingBalance(new OpeningBalance()
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .init(address1, 100));
        tester.openingBalance(new OpeningBalance()
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .init(address2, 20));

        tester.give(new Give()
                .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
                .address(address1)
                .init(address2, 0.0));

        System.out.println(wire);
    }

    @Test
    void testGiveOne() {
        test("give/one");
    }
}
