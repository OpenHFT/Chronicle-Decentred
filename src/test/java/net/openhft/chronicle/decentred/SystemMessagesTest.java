package net.openhft.chronicle.decentred;


import net.openhft.chronicle.core.Mocker;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.VerificationEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressEvent;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.address.InvalidationEvent;
import net.openhft.chronicle.decentred.dto.blockevent.EndOfRoundBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateChainRequest;
import net.openhft.chronicle.decentred.dto.chainlifecycle.CreateTokenRequest;
import net.openhft.chronicle.decentred.dto.error.ApplicationErrorResponse;
import net.openhft.chronicle.decentred.remote.rpc.RPCClient;
import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SystemMessagesTest {
    static BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    static RPCClient<SystemMessages, SystemMessages> client;
    static RPCServer<SystemMessages, SystemMessages> server;

    @BeforeAll
    public static void startServer() throws IOException {
        KeyPair ckp = new KeyPair(1);
        DtoRegistry<SystemMessages> dtoRegistry = DtoRegistry.newRegistry(SystemMessages.class);
        SystemMessages sm = Mocker.queuing(SystemMessages.class, "", queue);
        client = new RPCClient<>("client", "localhost", 10001, ckp.secretKey, dtoRegistry, sm, SystemMessages.class);

        KeyPair skp = new KeyPair(10001);
        server = new RPCServer<>("server", 10001, skp, SystemMessages.class, dtoRegistry, s ->
                (SystemMessages) Proxy.newProxyInstance(SystemMessages.class.getClassLoader(), new Class[]{SystemMessages.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getDeclaringClass() == Object.class)
                            return method.invoke(this, args);
                        if (args != null && args.length == 1)
                            method.invoke(s.toDefault(), args);
                        else
                            System.err.println("Unhandled message " + method);
                        return null;
                    }
                }));

    }

    @AfterAll
    public static void shutdown() {
        client.close();
        server.close();
    }

    @Test
    public void applicationError() {
        doTest(sm -> {
            InvalidationEvent message = new InvalidationEvent();
            sm.invalidationEvent(message);
            sm.applicationError(new ApplicationErrorResponse().init(message, "not supported"));
        }, 2);
    }

    @Test
    public void createAddressRequest() {
        doTest(sm -> sm.createAddressRequest(new CreateAddressRequest()), 1);
    }

    @Test
    public void createAddressEvent() {
        doTest(sm -> {
            CreateAddressRequest request = new CreateAddressRequest();
            sm.createAddressRequest(request);
            sm.createAddressEvent(new CreateAddressEvent().createAddressRequest(request));
        }, 2);
    }

    @Test
    public void createChainRequest() {
        doTest(sm -> sm.createChainRequest(new CreateChainRequest()), 1);
    }

    @Test
    public void createTokenRequest() {
        doTest(sm -> sm.createTokenRequest(new CreateTokenRequest()), 1);
    }

    @Test
    public void endOfRoundBlockEvent() {
        doTest(sm -> {
            EndOfRoundBlockEvent event = new EndOfRoundBlockEvent();
            event.addressToBlockNumberMap().justPut(1, 11);
            event.addressToBlockNumberMap().justPut(2, 22);
            sm.endOfRoundBlockEvent(event);
        }, 1);
    }

    @Test
    public void invalidationEvent() {
        doTest(sm -> sm.invalidationEvent(new InvalidationEvent()), 1);
    }

    @Test
    public void transactionBlockEvent() {
        doTest(sm -> sm.transactionBlockEvent(new TransactionBlockEvent()), 1);
    }

    @Test
    public void transactionBlockGossipEvent() {
        doTest(sm -> {
            TransactionBlockGossipEvent event = new TransactionBlockGossipEvent();
            event.addressToBlockNumberMap().justPut(1, 11);
            event.addressToBlockNumberMap().justPut(2, 22);
            sm.transactionBlockGossipEvent(event);
        }, 1);
    }

    @Test
    public void transactionBlockVoteEvent() {
        doTest(sm -> {
            TransactionBlockGossipEvent event = new TransactionBlockGossipEvent();
            event.addressToBlockNumberMap().justPut(1, 11);
            event.addressToBlockNumberMap().justPut(2, 22);
            sm.transactionBlockGossipEvent(event);
            TransactionBlockVoteEvent event2 = new TransactionBlockVoteEvent().gossipEvent(event);
            sm.transactionBlockVoteEvent(event2);
        }, 1);
    }

    @Test
    public void verificationEvent() {
        doTest(sm -> sm.verificationEvent(new VerificationEvent()), 1);
    }

    void doTest(Consumer<SystemMessages> csm, int count) {
        try {
            csm.accept(client.toDefault());
            for (int i = 0; i < count; i++) {
                assertNotNull("count: " + i + " missing",
                        queue.poll(1, TimeUnit.SECONDS));
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
}
