package town.lost.examples.appreciation.benchmark;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.VanillaBytes;
import net.openhft.chronicle.decentred.server.RPCBuilder;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.salt.Ed25519;

import static java.util.Objects.requireNonNull;

public abstract class Node<U extends T, T> {
    private final BytesStore privateKey;
    private final Bytes publicKey;
    private final Bytes secretKey;
    private final RPCBuilder<U, T> rpcBuilder;
    private final long address;

    public Node(long seed, Class<U> uClass, Class<T> tClass) {
        requireNonNull(uClass);
        requireNonNull(tClass);
        privateKey = DecentredUtil.testPrivateKey(seed);
        publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);

        address = DecentredUtil.toAddress(publicKey);

        rpcBuilder = RPCBuilder.of(17, uClass, tClass)
            .addClusterAddress(address)
            .secretKey(secretKey)
            .publicKey(publicKey);

   }

    public static long addressFromSeed(int seed) {  // TODO - convenient for bootstrapping seeded keys
        final BytesStore privateKey = DecentredUtil.testPrivateKey(seed);
        final Bytes publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        final Bytes secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);
        return  DecentredUtil.toAddress(publicKey);
    }

    public void addClusterAddress(long address) {
        rpcBuilder.addClusterAddress(address);
    }

    public long address() {
        return address;
    }

    public BytesStore getPrivateKey() {
        return privateKey;
    }

    public Bytes getPublicKey() {
        return publicKey;
    }

    public Bytes getSecretKey() {
        return secretKey;
    }

    public RPCBuilder<U, T> getRpcBuilder() {
        return rpcBuilder;
    }

    protected abstract void close();

}
