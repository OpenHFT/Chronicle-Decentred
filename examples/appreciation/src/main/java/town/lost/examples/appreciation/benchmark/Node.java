package town.lost.examples.appreciation.benchmark;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.VanillaBytes;
import net.openhft.chronicle.decentred.server.RPCBuilder;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.salt.Ed25519;

import static java.util.Objects.requireNonNull;

public abstract class Node<U extends T, T> {
    private final KeyPair keyPair;
    private final RPCBuilder<U, T> rpcBuilder;

    public Node(long seed, Class<U> uClass, Class<T> tClass) {
        requireNonNull(uClass);
        requireNonNull(tClass);

        keyPair = new KeyPair(seed);

        rpcBuilder = RPCBuilder.of(17, uClass, tClass)
            //.addClusterAddress(address) // Todo: Why do we add our own address?
            .keyPair(keyPair);

   }

    public static long addressFromSeed(int seed) {  // TODO - convenient for bootstrapping seeded keys
        KeyPair kp = new KeyPair(seed);
        return DecentredUtil.toAddress(kp.publicKey);
    }

    public void addClusterAddress(long address) {
        rpcBuilder.addClusterAddress(address);
    }

    public long address() {
        return keyPair.address();
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public RPCBuilder<U, T> getRpcBuilder() {
        return rpcBuilder;
    }

    protected abstract void close();

}
