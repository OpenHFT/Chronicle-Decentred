package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.core.pool.ClassAliasPool;
import net.openhft.chronicle.wire.Marshallable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeyPairTest {
    static {
        ClassAliasPool.CLASS_ALIASES.addAlias(KeyPair.class);
    }

    @Test
    public void marshallable() {
        KeyPair kp = new KeyPair(4);
        assertEquals("!KeyPair {\n" +
                "  publicKey: !!binary GGNLUWY17C7zCtzj1jB/dQ+YsFfAdmvLN+8cof3jggk=,\n" +
                "  secretKey: !!binary AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAAAAAAYY0tRZjXsLvMK3OPWMH91D5iwV8B2a8s37xyh/eOCCQ==\n" +
                "}\n", kp.toString());
        assertEquals("s.wc7vpqy2yw", DecentredUtil.toAddressString(kp.address()));
        KeyPair kp2 = Marshallable.fromString(kp.toString());
        assertEquals(kp.address(), kp2.address());
        assertEquals(kp, kp2);
    }
}
