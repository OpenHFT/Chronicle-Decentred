package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.base.DtoAliases;
import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;
import net.openhft.chronicle.decentred.internal.server.VanillaTransactionProcessor;
import net.openhft.chronicle.decentred.util.DtoRegistry;
import net.openhft.chronicle.decentred.util.KeyPair;
import net.openhft.chronicle.wire.TextMethodTester;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class VanillaTransactionProcessorTest {
    static {
        DtoAliases.addAliases();
    }

    public static void test(String basename) {
/*        try {
            for (String file : "setup.yaml,in.yaml,out.yaml".split(",")) {
                String path = "src/test/resources/" + basename;
                new File(path).mkdirs();
                try (Closeable c = new FileOutputStream(path + "/" + file, true)) {
                }
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }*/

        TextMethodTester<TransactionProcessorTester> tester = new TextMethodTester<>(
                basename + "/in.yaml",
                VanillaTransactionProcessorTest::createGateway,
                TransactionProcessorTester.class,
                basename + "/out.yaml");
        tester.setup(basename + "/setup.yaml");
//                .timeoutMS(Jvm.isDebug() ? 2_000 : 200);
        DtoRegistry<SystemMessages> dtoRegistry = DtoRegistry.newRegistry(SystemMessages.class);
        SetTimeProvider stp = new SetTimeProvider("2018-09-03T16:00:00.000001")
                .autoIncrement(1, TimeUnit.MICROSECONDS);
        KeyPair kp = new KeyPair('X');
        Consumer<Object[]> signer = args -> {
            if (args.length > 0 && args[0] instanceof VanillaSignedMessage) {
                VanillaSignedMessage sm = (VanillaSignedMessage) args[0];
                sm.protocol(dtoRegistry.protocolFor(sm.getClass()));
                sm.messageType(dtoRegistry.messageTypeFor(sm.getClass()));
                sm.sign(kp.secretKey, stp);
            }
        };
        tester.methodReaderInterceptor((m, o, args, invocation) -> {
            signer.accept(args);
            invocation.invoke(m, o, args);
        });
        tester.methodWriterListener((name, args) -> {
            signer.accept(args);
        });
        try {
            tester.run();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        assertEquals(tester.expected(), tester.actual());
    }

    static SystemMessages createGateway(TransactionProcessorTester tester) {
        VanillaTransactionProcessor vtp = new VanillaTransactionProcessor();
        vtp.messageRouter(tester);
        return vtp;
    }

    //    @Test TODO FIX
    void genesisOne() {
        test("genesis/one");
    }
}
