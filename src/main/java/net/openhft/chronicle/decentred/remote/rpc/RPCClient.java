package net.openhft.chronicle.decentred.remote.rpc;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.io.AbstractCloseable;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.time.TimeProvider;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.api.SystemMessageListener;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.remote.net.TCPClientListener;
import net.openhft.chronicle.decentred.remote.net.TCPConnection;
import net.openhft.chronicle.decentred.remote.net.VanillaTCPClient;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.DtoParser;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.LongObjMap;
import net.openhft.chronicle.wire.AbstractMethodWriterInvocationHandler;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class RPCClient<U extends T, T> extends AbstractCloseable implements TCPConnection, MessageRouter<U> {
    private final VanillaTCPClient tcpClient;
    private final T listener;
    private final BytesStore secretKey;
    private final DtoRegistry<U> registry;
    private final DtoParser<T> parser;
    private final U proxy;
    private boolean internal = false;
    private TimeProvider timeProvider = UniqueMicroTimeProvider.INSTANCE;

    public RPCClient(String name,
                     String socketHost,
                     int socketPort,
                     BytesStore secretKey,
                     DtoRegistry<U> registry,
                     T listener,
                     Class<T> tClass) {
        this(name, Collections.singletonList(new InetSocketAddress(socketHost, socketPort)), secretKey, registry, listener, tClass);
    }

    public RPCClient(String name,
                     List<InetSocketAddress> socketAddresses,
                     BytesStore secretKey,
                     DtoRegistry<U> registry,
                     T listener,
                     Class<T> tClass) {
        this.secretKey = secretKey;
        this.parser = registry.get(tClass);
        this.registry = registry;
        InvocationHandler handler = new AbstractMethodWriterInvocationHandler() {
            @Override
            protected void handleInvoke(Method method, Object[] args) {
                assert args.length == 1;
                VanillaSignedMessage vsm = (VanillaSignedMessage) args[0];
                write(vsm);
            }
        };
        Class<U> uClass = registry.superInterface();
        //noinspection unchecked
        proxy = (U) Proxy.newProxyInstance(uClass.getClassLoader(),
                new Class[]{uClass, SystemMessageListener.class},
                handler);
        this.listener = listener;
        this.tcpClient = new VanillaTCPClient(name, socketAddresses, new ClientListener());
    }

    @Override
    public U to(long address) { 
        return proxy;
    }

    public void write(VanillaSignedMessage message) { throwExceptionIfClosed();

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
    public void write(BytesStore<?, ByteBuffer> bytes) throws IOException { throwExceptionIfClosed();

 tcpClient.write(bytes);
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException { throwExceptionIfClosed();

 tcpClient.write(buffer);
    }

    @Override
    protected void performClose() {
        tcpClient.close();
    }

    public boolean internal() { 
        return internal;
    }

    public RPCClient<U, T> internal(boolean internal) {
        this.internal = internal;
        return this;
    }

    public TimeProvider timeProvider() { throwExceptionIfClosed();

 return timeProvider;
    }

    public RPCClient<U, T> timeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        return this;
    }

    class ClientListener implements TCPClientListener {

        @Override
        public void onMessage(TCPConnection client, Bytes bytes) throws IOException { throwExceptionIfClosed();

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
