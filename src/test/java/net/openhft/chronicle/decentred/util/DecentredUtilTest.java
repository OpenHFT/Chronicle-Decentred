package net.openhft.chronicle.decentred.util;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.decentred.dto.CreateAddressRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DecentredUtilTest {
/*
-982	127.178.122.86:22851:61d2
-898	127.153.96.30:2296:7e10
 142	127.102.155.73:26065:bc79
 229	127.162.254.71:54087:781
 602	127.27.187.195:45455:a9ac
 849	127.180.252.252:59774:fa8e
 981	127.182.134.255:60203:ec86
1309	127.99.25.162:7666:48ae
1417	127.120.76.156:18091:2e5e
2570	127.224.106.149:27274:78
2719	127.131.134.140:49418:fccc
2727	127.104.121.212:32800:9230
2770	127.155.158.58:29446:fa6f
2836	127.134.190.8:45000:d9c2
2859	127.208.107.172:6384:ecb8
3563	127.250.233.76:26897:b71a
3717	127.136.150.117:35689:14ab
3765	127.100.190.99:16964:1b3b
4179	127.228.94.41:37879:d3d7
4320	127.72.253.210:53100:a2d9
4878	127.164.194.15:45140:a964
4966	127.170.42.128:53890:a9aa
5288	127.52.90.136:43764:ec14
5397	127.254.10.43:24809:6048
5479	127.139.124.163:39419:e9ee
5828	127.3.87.87:34513:3d5
6092	127.230.183.34:17915:e379
6105	127.114.23.74:20048:dc1b
6558	127.250.117.168:27430:9810
6591	127.53.160.130:36353:1201
6635	127.15.168.31:32960:4806
6789	127.161.165.173:28814:883b
6864	127.251.46.224:35980:2dbf
7310	127.169.137.50:15068:6ef2
7442	127.163.88.79:55574:1a71
7743	127.117.136.179:54809:18af
7958	127.101.113.101:52840:38d1
7980	127.60.93.106:36048:f16
8607	127.253.117.209:25604:b2a5
8711	127.99.27.202:39927:f85f
9192	127.152.200.26:26677:c697
9857	127.42.158.246:64731:108b
     */

    public static void main(String[] args) {
        KeyPair kp = new KeyPair('X');
        CreateAddressRequest car0 = new CreateAddressRequest()
                .protocol(1).messageType(1)
                .publicKey(kp.publicKey)
                .sign(kp.secretKey);
        System.out.println(car0);

        // generate user addresses
        for (int i = -999; i < 10000; i++) {
            KeyPair kp7 = new KeyPair(i);
            BytesStore publicKey = kp7.publicKey;
            BytesStore secretKey = kp7.secretKey;

            long address = DecentredUtil.toAddress(publicKey);
            if ((address >> 56) == 127) {
                System.out.println(i + "\t" + DecentredUtil.toAddressString(address));
                CreateAddressRequest car = new CreateAddressRequest()
                        .protocol(1).messageType(1)
                        .publicKey(publicKey)
                        .sign(secretKey);
                System.out.println(car);
            }
        }
    }

    @Test
    public void testPrivateKey() {
        KeyPair kp7 = new KeyPair(0);
        BytesStore publicKey1 = kp7.publicKey;
        BytesStore secretKey1 = kp7.secretKey;

        KeyPair kp7b = new KeyPair(0);
        BytesStore publicKey2 = kp7b.publicKey;
        BytesStore secretKey2 = kp7b.secretKey;

        assertEquals(publicKey2, publicKey1);
        assertEquals(secretKey2, secretKey1);
    }
}