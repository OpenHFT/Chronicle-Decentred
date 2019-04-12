package net.openhft.chronicle.decentred.remote.net;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class VanillaTCPClient extends AbstractTCPConnection {
    private final List<InetSocketAddress> socketAddresses = new ArrayList<>();
    private final Thread thread;
    private final TCPClientListener clientListener;
    private int nextChannel = -1;
    private long failed = 0;

    public VanillaTCPClient(String name, List<InetSocketAddress> socketAddresses, TCPClientListener clientListener) {
        this.clientListener = clientListener;
        this.socketAddresses.addAll(socketAddresses);
        thread = new Thread(this::run, name);
        thread.start();
    }

    @Override
    protected void close2() {
        if (thread != null) {
            thread.interrupt();
            Thread.yield();
        }
    }

    @Override
    protected void waitForReconnect() throws IOException {
        int count = 0;
        while (channel == null) {
            Jvm.pause(1);
            if (++count > 1000)
                throw new IOException("not open yet:" + thread.getName());
        }
    }

    @Override
    protected void onMessage(Bytes<ByteBuffer> bytes) throws IOException {
        clientListener.onMessage(this, bytes);
    }

    void run() {
        Bytes<ByteBuffer> readBytes = Bytes.elasticByteBuffer(MAX_MESSAGE_SIZE);
        try {
            while (running) {
                if (channel == null || !channel.isOpen()) {
                    readBytes.clear();
                    openChannel();
                    clientListener.onNewConnection(this, channel);
                } else {
                    try {
                        readChannel(readBytes);

                    } catch (IOException ioe) {
                        Jvm.pause(1);
                        if (running)
                            System.out.println("closed client channel = " + this.channel);
                            // ioe.printStackTrace();
                        channel.close();
                        clientListener.onClosedChannel(this);
                    }
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            readBytes.release();
        }
    }

    private void openChannel() {
        if (++nextChannel >= socketAddresses.size())
            nextChannel = 0;
        InetSocketAddress socketAddress = socketAddresses.get(nextChannel);
        try {
            channel(SocketChannel.open(socketAddress));
            Socket socket = channel.socket();
            socket.setReceiveBufferSize(1 << 20);
//            socket.setTcpNoDelay(true);
            System.out.println("Connected to " + socketAddress);
            failed = 0;

        } catch (IOException ioe) {
            failed++;
            if (failed > socketAddresses.size()) {
                System.err.println("Failed to connect to " + socketAddress + " trying again. " + ioe);
            }
            long delay = Math.min(1000, 100 * failed / socketAddresses.size());
            if (delay > 0)
                Jvm.pause(delay);
            channel(null);
        }
    }
}
