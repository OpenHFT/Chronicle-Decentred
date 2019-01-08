package net.openhft.chronicle.decentred.remote.rpc;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.SystemMessageListener;
import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.remote.net.TCPClientListener;
import net.openhft.chronicle.decentred.remote.net.TCPConnection;
import net.openhft.chronicle.decentred.remote.net.VanillaTCPClient;
import net.openhft.chronicle.decentred.util.*;
import net.openhft.chronicle.wire.AbstractMethodWriterInvocationHandler;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class RPCClient<T> implements Closeable, TCPConnection, MessageRouter<T> {
    private final VanillaTCPClient tcpClient;
    private final T listener;
    private final BytesStore secretKey;
    private final DtoRegistry<T> registry;
    private final DtoParser<T> parser;
    private final LongObjMap<BytesStore> addressToPublicKey =
            LongObjMap.withExpectedSize(BytesStore.class, 16);
    private final T proxy;
    private boolean internal = false;
    private TimeProvider timeProvider = UniqueMicroTimeProvider.INSTANCE;

    public RPCClient(String name,
                     String socketHost,
                     int socketPort,
                     BytesStore secretKey,
                     DtoRegistry<T> registry,
                     T listener) {
        this(name, Collections.singletonList(new InetSocketAddress(socketHost, socketPort)), secretKey, registry, listener);
    }

    public RPCClient(String name,
                     List<InetSocketAddress> socketAddresses,
                     BytesStore secretKey,
                     DtoRegistry<T> registry,
                     T listener) {
        this.secretKey = secretKey;
        this.parser = registry.get();
        this.registry = registry;
        InvocationHandler handler = new AbstractMethodWriterInvocationHandler() {
            @Override
            protected void handleInvoke(Method method, Object[] args) {
                assert args.length == 1;
                VanillaSignedMessage vsm = (VanillaSignedMessage) args[0];
                write(vsm);
            }
        };
        //noinspection unchecked
        Class<T> tClass = registry.superInterface();
        proxy = (T) Proxy.newProxyInstance(tClass.getClassLoader(),
                new Class[]{tClass, SystemMessageListener.class},
                handler);
        this.listener = listener;
        this.tcpClient = new VanillaTCPClient(name, socketAddresses, new ClientListener());
    }

    @Override
    public T to(long address) {
        return proxy;
    }

    public void write(VanillaSignedMessage message) {
        try {
            if (message.protocol() == 0) {
                int pmt = registry.protocolMessageTypeFor(message.getClass());
                message.protocol(pmt >>> 16);
                message.messageType(pmt & DecentredUtil.MASK_16);
            }
            if (!message.signed()) {
                message.sign(secretKey, timeProvider);
            }
            tcpClient.write(message.byteBuffer());

        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public void write(BytesStore<?, ByteBuffer> bytes) throws IOException {
        tcpClient.write(bytes);
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
        tcpClient.write(buffer);
    }

    @Override
    public void close() {
        tcpClient.close();
    }

    public boolean internal() {
        return internal;
    }

    public RPCClient internal(boolean internal) {
        this.internal = internal;
        return this;
    }

    public TimeProvider timeProvider() {
        return timeProvider;
    }

    public RPCClient<T> timeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        return this;
    }

    class ClientListener implements TCPClientListener {

        @Override
        public void onMessage(TCPConnection client, Bytes bytes) throws IOException {
            bytes.readSkip(-4);
            try {
                parser.parseOne(bytes, listener);

            } catch (IORuntimeException iore) {
                if (iore.getCause() instanceof IOException)
                    throw (IOException) iore.getCause();
                throw iore;
            }
        }
    }
}
