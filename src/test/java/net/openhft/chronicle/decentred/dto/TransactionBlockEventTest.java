package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Mocker;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.decentred.api.AddressManagementRequests;
import net.openhft.chronicle.decentred.api.ConnectionStatusListener;
import net.openhft.chronicle.decentred.api.SystemMessageListener;
import net.openhft.chronicle.decentred.api.SystemMessages;
import net.openhft.chronicle.decentred.dto.address.CreateAddressRequest;
import net.openhft.chronicle.decentred.dto.base.DtoAliases;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockGossipEvent;
import net.openhft.chronicle.decentred.dto.blockevent.TransactionBlockVoteEvent;
import net.openhft.chronicle.decentred.util.*;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TransactionBlockEventTest {
    static {
        DtoAliases.addAliases();
    }

    @Test
    final void writeMarshallable() {
        KeyPair kp = new KeyPair(7);
        KeyPair kp2 = new KeyPair(17);

        DtoRegistry<SystemMessages> registry = DtoRegistry.newRegistry(SystemMessages.class)
                .addProtocol(1, SystemMessageListener.class)
                .addProtocol(2, AddressManagementRequests.class)
                .addProtocol(3, ConnectionStatusListener.class);
        @SuppressWarnings("unchecked")
        TransactionBlockEvent<SystemMessages> tbe = registry.create(TransactionBlockEvent.class);
        tbe.timestampUS(1534769584076123L);
        tbe.dtoParser(registry.get());
        tbe.addTransaction(
                registry.create(CreateAddressRequest.class)
                        .sign(kp.secretKey, new SetTimeProvider("2018-08-20T12:53:04.075128")));
        tbe.addTransaction(
                registry.create(CreateAddressRequest.class)
                        .sign(kp2.secretKey, new SetTimeProvider("2018-08-20T12:53:04.075256")));
        tbe.sign(kp.secretKey, new SetTimeProvider("2018-08-20T12:53:04.076123"));
        assertEquals("!TransactionBlockEvent {\n" +
                "  timestampUS: 2018-08-20T12:53:04.076123,\n" +
                "  address: nphccofmpy6ci,\n" +
                "  chainAddress: .,\n" +
                "  transactions: [\n" +
                "    !CreateAddressRequest { timestampUS: 2018-08-20T12:53:04.075128, address: nphccofmpy6ci, publicKey: !!binary 9M9t8hyt2kEJmL46Fs+si0VigLTMQt9OafgMm3ljIOg= },\n" +
                "    !CreateAddressRequest { timestampUS: 2018-08-20T12:53:04.075256, address: nud6jbceicts2, publicKey: !!binary TsXED8x8VoxtLgRu7iPaz4aAhfQUtmvee9KRyhDKk+o= }\n" +
                "  ]\n" +
                "}\n", tbe.toString());
        System.out.println(tbe);

/*
        TransactionBlockEvent tbe2 = Marshallable.fromString(tbe.toString());
        assertEquals(tbe2, tbe);
        assertEquals(
                "0000 52 01 00 00                                     # length\n" +
                        "0004 6e ca c1 7a 26 95 26 7c d5 aa 7f cf 65 b3 97 f1 # signature start\n" +
                        "0014 91 df 1a 72 9b 75 ff e1 af d3 d6 dd 84 0e 9b 3b\n" +
                        "0024 2b 09 15 f0 27 17 8a 43 03 27 35 39 13 f9 f9 25\n" +
                        "0034 95 80 00 36 61 f4 9b 82 74 ee f9 25 ba 52 cf 06 # signature end\n" +
                        "0044 f0 ff                                           # messageType\n" +
                        "0046 ff ff                                           # protocol\n" +
                        "0048    5b f5 de 63 dd 73 05 00                         # timestampUS\n" +
                        "0050    69 f8 0c 9b 79 63 20 e8                         # address\n" +
                        "0058    00 00 00 00 00 00 00 00                         # chainAddress\n" +
                        "0060 79 00 00 00 a3 a7 8c 44 79 1b 1e 7d 3c 0a fc c0\n" +
                        "0070 fd a0 98 f5 69 4d 3f e6 6e fc 1e 2d 34 a5 39 84\n" +
                        "0080 43 41 6d 40 10 03 a6 ee 05 74 02 a9 86 8c de d2\n" +
                        "0090 36 a4 df b8 ea 3a 21 21 c3 ed 0f b5 f9 76 d6 51\n" +
                        "00a0 60 66 40 0e 00 f0 02 00 78 f1 de 63 dd 73 05 00\n" +
                        "00b0 69 f8 0c 9b 79 63 20 e8 20 f4 cf 6d f2 1c ad da\n" +
                        "00c0 41 09 98 be 3a 16 cf ac 8b 45 62 80 b4 cc 42 df\n" +
                        "00d0 4e 69 f8 0c 9b 79 63 20 e8 79 00 00 00 1c db e3\n" +
                        "00e0 7e 08 b9 33 cc 42 35 20 78 8b 27 8e 19 07 4b b8\n" +
                        "00f0 70 7f 63 55 4b 3b 52 03 55 f1 bb e2 f9 8e 7a 2c\n" +
                        "0100 f3 64 3d b9 ea c8 96 82 bf 95 72 3b 6e c8 ab 95\n" +
                        "0110 81 09 29 e0 44 f2 71 9c 3f 2b d9 18 0f 00 f0 02\n" +
                        "0120 00 f8 f1 de 63 dd 73 05 00 7b d2 91 ca 10 ca 93\n" +
                        "0130 ea 20 4e c5 c4 0f cc 7c 56 8c 6d 2e 04 6e ee 23\n" +
                        "0140 da cf 86 80 85 f4 14 b6 6b de 7b d2 91 ca 10 ca\n" +
                        "0150 93 ea\n", tbe.toHexString());
  */


        TransactionBlockGossipEvent gossip = registry.create(TransactionBlockGossipEvent.class)
                //.blockNumber(1)
                .chainAddress(DecentredUtil.parseAddress("local"));
        LongLongMap map = gossip.addressToBlockNumberMap();
        map.justPut(DecentredUtil.parseAddress("xxx"), 123);
        map.justPut(DecentredUtil.parseAddress("yyy"), 109);
        map.justPut(DecentredUtil.parseAddress("zzz"), 195);
        gossip.sign(kp.secretKey, new SetTimeProvider("2018-08-20T12:53:04.076123"));
        assertEquals("!TransactionBlockGossipEvent {\n" +
                "  timestampUS: 2018-08-20T12:53:04.076123,\n" +
                "  address: nphccofmpy6ci,\n" +
                "  chainAddress: local,\n" +
                "  addressToBlockNumberMap: {\n" +
                "    xxx: 123,\n" +
                "    yyy: 109,\n" +
                "    zzz: 195\n" +
                "  }\n" +
                "}\n", gossip.toString());

/*
        TransactionBlockGossipEvent gossip2 = Marshallable.fromString(gossip.toString());
        assertEquals(gossip, gossip2);
        assertEquals(
                "0000 8d 00 00 00                                     # length\n" +
                        "0004 91 e4 89 21 f8 63 45 93 9e 88 e4 c1 3d 88 bd da # signature start\n" +
                        "0014 b7 0c 78 8a fd a3 0f e0 8e e8 6a fc d7 47 4d cb\n" +
                        "0024 80 fe 15 01 48 48 06 f2 1d e7 91 0b c9 dd 16 5a\n" +
                        "0034 01 50 81 ed 39 f4 29 99 49 8d 39 3d 37 c4 c6 0b # signature end\n" +
                        "0044 f1 ff                                           # messageType\n" +
                        "0046 ff ff                                           # protocol\n" +
                        "0048    5b f5 de 63 dd 73 05 00                         # timestampUS\n" +
                        "0050    69 f8 0c 9b 79 63 20 e8                         # address\n" +
                        "0058    2c 8c c7 00 00 00 00 00                         # chainAddress\n" +
                        "0060 2c 8c c7 00 00 00 00 00 03 39 67 00 00 00 00 00\n" +
                        "0070 00 6d 00 00 00 5a 6b 00 00 00 00 00 00 c3 00 00\n" +
                        "0080 00 18 63 00 00 00 00 00 00 7b 00 00 00\n", gossip.toHexString());
*/

/* TODO FIX
        Wire wire = new BinaryWire(Bytes.allocateElasticDirect(1 << 11));

        TransactionBlockEvent<SystemMessages> tbe3 = registry.create(TransactionBlockEvent.class);

        tbe.writeMarshallable(wire);
        //gossip.writeMarshallable(wire);
        tbe3.readMarshallable(wire);
        //gossip2.readMarshallable(wire);
        assertEquals(gossip.toString(), gossip2.toString());
*/

        System.out.println(gossip);

        TransactionBlockVoteEvent vote = registry.create(TransactionBlockVoteEvent.class)
                .gossipEvent(gossip);
        vote.sign(kp.secretKey, new SetTimeProvider("2018-08-20T12:53:04.761234"));
        System.out.println(vote);

    }

    @Test
    void testTbeMarshall() {
        KeyPair kp = new KeyPair(7);
        KeyPair kp2 = new KeyPair(17);

        DtoRegistry<SystemMessages> registry = DtoRegistry.newRegistry(SystemMessages.class)
                .addProtocol(1, SystemMessageListener.class)
                .addProtocol(2, AddressManagementRequests.class)
                .addProtocol(3, ConnectionStatusListener.class);
        @SuppressWarnings("unchecked")
        TransactionBlockEvent<SystemMessages> tbe = registry.create(TransactionBlockEvent.class);
        tbe.timestampUS(1534769584076123L);
        tbe.dtoParser(registry.get());

        tbe.addTransaction(
                registry.create(CreateAddressRequest.class)
                        .sign(kp.secretKey, new SetTimeProvider("2018-08-20T12:53:04.075128")));
        tbe.addTransaction(
                registry.create(CreateAddressRequest.class)
                        .sign(kp2.secretKey, new SetTimeProvider("2018-08-20T12:53:04.075256")));

        tbe.sign(kp.secretKey, new SetTimeProvider("2018-08-20T12:53:04.076123"));

        TransactionBlockEvent<SystemMessages> tbe2 = registry.create(TransactionBlockEvent.class);
        //Wire wire = new BinaryWire(Bytes.allocateElasticDirect(1 << 11));
        Wire wire = new TextWire(Bytes.allocateElasticDirect(1 << 11)); // works with this
        tbe.writeMarshallable(wire);

        tbe2.readMarshallable(wire);

        assertEquals(tbe.toString(), tbe2.toString());

        System.out.println("wire = " + wire);

    }

    @Test
    void testBytesMarshall() {
        KeyPair kp = new KeyPair(7);
        KeyPair kp2 = new KeyPair(17);

        DtoRegistry<SystemMessages> registry = DtoRegistry.newRegistry(SystemMessages.class)
                .addProtocol(1, SystemMessageListener.class)
                .addProtocol(2, AddressManagementRequests.class)
                .addProtocol(3, ConnectionStatusListener.class);
        @SuppressWarnings("unchecked")
        TransactionBlockEvent<SystemMessages> tbe = registry.create(TransactionBlockEvent.class);
        tbe.timestampUS(1534769584076123L);

        tbe.addTransaction(
                registry.create(CreateAddressRequest.class)
                        .sign(kp.secretKey, new SetTimeProvider("2018-08-20T12:53:04.075128")));
        tbe.addTransaction(
                registry.create(CreateAddressRequest.class)
                        .sign(kp2.secretKey, new SetTimeProvider("2018-08-20T12:53:04.075256")));

        tbe.sign(kp.secretKey, new SetTimeProvider("2018-08-20T12:53:04.076123"));

        TransactionBlockEvent<SystemMessages> tbe2 = registry.create(TransactionBlockEvent.class);
        Bytes bytes = Bytes.allocateElasticDirect(1 << 11);

        tbe.writeMarshallable(bytes);

        tbe2.readMarshallable(bytes);
        tbe2.dtoParser(registry.get());
        tbe2.replay(Mocker.logging(SystemMessages.class, "tbe2: ", System.out));

        // so we can dump the contents as strings
        tbe.dtoParser(registry.get());
        tbe2.dtoParser(registry.get());
        assertEquals(tbe.toString(), tbe2.toString());
    }
}