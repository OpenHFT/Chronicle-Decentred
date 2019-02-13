package town.lost.examples.appreciation.benchmark;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.VanillaBytes;
import net.openhft.chronicle.decentred.server.RPCBuilder;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.salt.Ed25519;

public abstract class Node<U extends T, T> {
    private final BytesStore privateKey;
    private final VanillaBytes<Void> publicKey;
    private final VanillaBytes<Void> secretKey;
    private final RPCBuilder<U, T> rpcBuilder;

    public Node(int seed, Class<U> uClass, Class<T> tClass) {
        privateKey = DecentredUtil.testPrivateKey(seed);
        publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(this.publicKey, this.secretKey, this.privateKey);

        rpcBuilder = RPCBuilder.of(17, uClass, tClass)
            .addClusterAddress(DecentredUtil.toAddress(this.publicKey))
            .secretKey(this.secretKey)
            .publicKey(this.publicKey);

    }

    public static long addressFromSeed(int seed) {
        BytesStore privateKey = DecentredUtil.testPrivateKey(seed);
        VanillaBytes<Void> publicKey = Bytes.allocateDirect(Ed25519.PUBLIC_KEY_LENGTH);
        VanillaBytes<Void> secretKey = Bytes.allocateDirect(Ed25519.SECRET_KEY_LENGTH);
        Ed25519.privateToPublicAndSecret(publicKey, secretKey, privateKey);
        return  DecentredUtil.toAddress(publicKey);
    }

    public long address() {
        return DecentredUtil.toAddress(publicKey);
    }

    public BytesStore getPrivateKey() {
        return privateKey;
    }

    public VanillaBytes<Void> getPublicKey() {
        return publicKey;
    }

    public VanillaBytes<Void> getSecretKey() {
        return secretKey;
    }

    public RPCBuilder<U, T> getRpcBuilder() {
        return rpcBuilder;
    }

    abstract void close();

}
