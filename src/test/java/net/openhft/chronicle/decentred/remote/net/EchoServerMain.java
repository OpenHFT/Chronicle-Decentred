package net.openhft.chronicle.decentred.remote.net;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.io.AbstractCloseable;
import net.openhft.chronicle.core.io.Closeable;

import java.io.IOException;

class EchoServerMain extends AbstractCloseable implements Closeable, TCPServerConnectionListener {
    static final int PORT = Integer.getInteger("port", 9090);

    private final VanillaTCPServer server;

    EchoServerMain(String name, int port) throws IOException {
        server = new VanillaTCPServer(name, port, this);
    }

    public static void main(String... args) throws IOException {
        new EchoServerMain("echo", PORT);
    }

    @Override
    public void onNewConnection(TCPServer server, TCPConnection channel) {
        System.out.println("Connected " + channel);
    }

    @Override
    public void onClosedConnection(TCPServer server, TCPConnection channel) {
        System.out.println("... Disconnected " + channel);
    }

    public void onMessage(TCPServer server, TCPConnection channel, Bytes bytes) throws IOException {
        System.out.println("+ " + bytes);
        channel.write(bytes);
    }

    @Override
    protected void performClose() {
        server.close();
    }
}
