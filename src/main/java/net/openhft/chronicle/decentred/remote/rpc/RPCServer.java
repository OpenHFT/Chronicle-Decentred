package net.openhft.chronicle.decentred.remote.rpc;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.decentred.api.SystemMessageListener;
import net.openhft.chronicle.decentred.dto.SignedMessage;
import net.openhft.chronicle.decentred.dto.VanillaSignedMessage;
import net.openhft.chronicle.decentred.remote.net.TCPConnection;
import net.openhft.chronicle.decentred.remote.net.TCPServer;
import net.openhft.chronicle.decentred.remote.net.TCPServerConnectionListener;
import net.openhft.chronicle.decentred.remote.net.VanillaTCPServer;
import net.openhft.chronicle.decentred.server.DecentredServer;
import net.openhft.chronicle.decentred.util.*;
import net.openhft.chronicle.wire.AbstractMethodWriterInvocationHandler;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class RPCServer<U extends T, T> implements DecentredServer<U>, Closeable {
    private static final ThreadLocal<TCPConnection> DEFAULT_CONNECTION_TL = new ThreadLocal<>();
    private final LongObjMap<TCPConnection> connections = LongObjMap.withExpectedSize(TCPConnection.class, 128);
    private final LongObjMap<TCPConnection> remoteMap = LongObjMap.withExpectedSize(TCPConnection.class, 128);
    private final Map<Long, U> allMessagesMap = new ConcurrentHashMap<>();
    private final PublicKeyRegistry publicKeyRegistry = new VanillaPublicKeyRegistry();
    private final TCPServer tcpServer;
    private final long address;
    private final BytesStore publicKey;
    private final BytesStore secretKey;
    private final Class<T> tClass;
    private final DtoRegistry<U> dtoRegistry;
    private final T serverComponent;

    public RPCServer(String name,
                     int port,
                     long address,
                     BytesStore publicKey,
                     BytesStore secretKey,
                     Class<T> tClass,
                     DtoRegistry<U> dtoRegistry,
                     Function<DecentredServer<U>, T> serverComponentBuilder) throws IOException {
        this.address = address;
        this.publicKey = publicKey;
        this.secretKey = secretKey;
        this.tClass = tClass;
        this.dtoRegistry = dtoRegistry;
        tcpServer = new VanillaTCPServer(name, port, new XCLConnectionListener(dtoRegistry.get(tClass)));
        this.serverComponent = serverComponentBuilder.apply(this);
    }

    public int getPort() {
        return tcpServer.getPort();
    }

    @Override
    public void register(long address, BytesStore publicKey) {
        publicKeyRegistry.register(address, publicKey);
    }

    @Override
    public Boolean verify(long address, BytesStore sigAndMsg) {
        return publicKeyRegistry.verify(address, sigAndMsg);
    }

    public boolean internal() {
        return publicKeyRegistry.internal();
    }

    public RPCServer<U, T> internal(boolean internal) {
        publicKeyRegistry.internal(internal);
        return this;
    }

    /**
     * Add known connections between clusters
     *
     * @param addressOrRegion to associate with this connection
     * @param tcpConnection   to connect to.
     */
    public void addTCPConnection(long addressOrRegion, TCPConnection tcpConnection) {
        System.out.println("Registered " + DecentredUtil.toAddressString(addressOrRegion) + " as " + tcpConnection);
        synchronized (remoteMap) {
            remoteMap.justPut(addressOrRegion, tcpConnection);
        }
    }

    @Override
    public void subscribe(long address) {
        addTCPConnection(address, DEFAULT_CONNECTION_TL.get());
    }

    @Override
    public U to(long addressOrRegion) {
        InvocationHandler handler = new ServerInvocationHandler(addressOrRegion);
        Class<U> uClass = dtoRegistry.superInterface();
        //noinspection unchecked
        U proxy = (U) Proxy.newProxyInstance(uClass.getClassLoader(),
                new Class[]{uClass, SystemMessageListener.class},
                handler);
        return proxy;
    }

    private long address() {
        return address;
    }

    @Override
    public void close() {
        synchronized (connections) {
            connections.forEach((k, connection) ->
                    Closeable.closeQuietly(connection));
            connections.clear();
        }
        synchronized (remoteMap) {
            remoteMap.forEach((k, connection) ->
                    Closeable.closeQuietly(connection));
            remoteMap.clear();
        }
        tcpServer.close();
    }

    @Override
    public synchronized void onMessageTo(long address, SignedMessage message) {
        System.out.println(Thread.currentThread().getName() + " to " + DecentredUtil.toAddressString(address) + " " + message);
        write(address, message);
    }

    void write(long toAddress, SignedMessage message) {
        TCPConnection tcpConnection;
        if (toAddress == 0) {
            tcpConnection = DEFAULT_CONNECTION_TL.get();
        } else {
            synchronized (connections) {
                tcpConnection = connections.get(toAddress);
            }
            if (tcpConnection == null) {
                synchronized (remoteMap) {
                    tcpConnection = remoteMap.get(toAddress);
                }
            }
        }

        if (tcpConnection == null) {
            String addressString = DecentredUtil.toAddressString(toAddress);
            System.out.println(address + " - No connection to address " + addressString + " to send " + message);
            return;
        }

        try {

            if (!message.signed()) {
                if (message.protocol() == 0) {
                    int protocol = dtoRegistry.protocolFor(message.getClass());
                    int messageType = dtoRegistry.messageTypeFor(message.getClass());
                    ((VanillaSignedMessage) message).protocol(protocol).messageType(messageType);
                }
                message.sign(secretKey);
            }
            tcpConnection.write(((VanillaSignedMessage) message).byteBuffer());

        } catch (IllegalStateException e2) {
            e2.printStackTrace();
            System.err.println("Failed to marshall object " + e2.toString());
            // we should never get IllegalStateException exceptions, but if we do,
            // rethrow the exception so that the LocalPostBlockChainProcessor can send a RequestFailedEvent back to the Client
            throw e2;
        } catch (Exception e) {
            e.printStackTrace();
            // assume it's dead.
            Closeable.closeQuietly(tcpConnection);
            synchronized (connections) {
                connections.justRemove(toAddress);
            }
            Jvm.warn().on(getClass(), "Exception while sending message to: " + toAddress + ", message: " + message, e);
        }
    }

    class XCLConnectionListener implements TCPServerConnectionListener {
        final DtoParser<T> dtoParser;

        XCLConnectionListener(DtoParser<T> dtoParser) {
            this.dtoParser = dtoParser;
        }

        @Override
        public void onMessage(TCPServer server, TCPConnection channel, Bytes bytes) throws IOException {
            DEFAULT_CONNECTION_TL.set(channel);
            bytes.readSkip(-4);
            try {

                long sourceAddress = dtoParser.parseOne(bytes, serverComponent);

                if (!connections.containsKey(sourceAddress)) { // Todo -- fix this way of adding connections. This is wrong
                    synchronized (connections) {
                        System.out.println("Associating address " + sourceAddress + " to " + channel);
                        connections.justPut(sourceAddress, channel);
                    }
                }
            } catch (IORuntimeException iore) {
                if (iore.getCause() instanceof IOException)
                    throw (IOException) iore.getCause();
                throw iore;
            }
        }
    }

    class ServerInvocationHandler extends AbstractMethodWriterInvocationHandler {
        final long addressOrRegion;

        ServerInvocationHandler(long addressOrRegion) {
            this.addressOrRegion = addressOrRegion;
        }

        @Override
        protected void handleInvoke(Method method, Object[] args) {
            assert args.length == 1;
            VanillaSignedMessage vsm = (VanillaSignedMessage) args[0];
            write(addressOrRegion, vsm);
        }
    }
}
