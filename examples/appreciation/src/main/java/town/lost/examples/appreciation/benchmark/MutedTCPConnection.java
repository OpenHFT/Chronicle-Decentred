package town.lost.examples.appreciation.benchmark;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.remote.net.TCPConnection;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class MutedTCPConnection implements TCPConnection {

    private static final TCPConnection INSTANCE = new MutedTCPConnection();

    private MutedTCPConnection() {}

    @Override
    public void write(BytesStore<?, ByteBuffer> bytes) throws IOException {}

    @Override
    public void write(ByteBuffer buffer) throws IOException {}

    @Override
    public void close() {}

    public static TCPConnection get() {
        return INSTANCE;
    }
}
