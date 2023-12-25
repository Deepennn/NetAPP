package com.netapp;

import com.netapp.config.NetFactory;
import com.netapp.net.Net;

import java.util.Scanner;

import static com.netapp.config.DeviceConfig.*;

public class APP {
    public static Net net;
    public static void setup(){
        // 创建网络
        net = NetFactory.provide();
        // 启动网络
        new Thread(net).start();
    }
    public static void service(){
        // 创建Scanner
        Scanner scanner = new Scanner(System.in);
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        System.out.println("    WELCOME TO NET LAYER!");
        System.out.println(
                "    ```````````````````NET TOPO```````````````````````\n" +
                "    h1   <===>         r1        <===> h2\n" +
                "    h1_i <---> [ r1_i1 , r1_i2 ] <---> h2_i\n" +
                "   ```````````````````````````````````````````````````"
        );
        System.out.print(
                "    @TEST: h1 -> r1 ->h2\n" +
                "    YOU ARE NOW `h1` IN THE NET TOPO AS ABOVE\n" +
                "    TRY SEND A MESSAGE TO `h2`!\n" +
                "    INPUT YOUR(`h1`'s) MESSAGE HERE: "
        );
        // 读取用户输入的 MESSAGE
        String message = scanner.nextLine();
        System.out.println("/**```````````````````YOUR MESSAGE IS SENDING!````````````````*/");

        /**------------------TEST ONLY ONE `@TEST` EACH TIME------------------------------*/
        /**
         * h1 -> h2               @TEST: h1 -> r1 ->h2
         * */
        net.service.sendIPPacket(H1_HOSTNAME, H2_I_IP, message, 64);

        /**
         * h2 -> h1               @TEST: h2 -> r1 ->h1
         * */
//        net.service.sendIPPacket(H2_HOSTNAME, H1_I_IP, message, 64);

        /**
         * h1 -> r1               @TEST: h1 -> r1
         * */
//        net.service.sendIPPacket(H1_HOSTNAME, R1_I1_IP, message, 64);

        /**
         *  h1 -> ?                @TEST: ICMP_DESTINATION_NETWORK_UNREACHABLE
         * */
//        net.service.sendIPPacket(H1_HOSTNAME,"1.1.1.1", message, 64);

        /**
         *  h1 -> h2               @TEST: ICMP_DESTINATION_TIME_EXCEEDED
         *  */
//        net.service.sendIPPacket(H1_HOSTNAME, H2_I_IP, message, 1);

        /**
         *  h1 ?-> h2              @TEST: ARP_REQ
         *  NOTIFICATION:
         *  DELETE THE ENTRY `1.0.0.1 00:11:22:33:44:55`
         *  IN THE FILE `src/main/resources/config/arp_cache/h1.ac`
         *  MANUALLY,
         *  MAKE SURE THERE IS <=1 BLANK LINES AT THE BOTTOM
         *  AND TRY THIS AGAIN:
         *  */
//        net.service.sendIPPacket(H1_HOSTNAME, H2_I_IP, message, 64);

        // 关闭Scanner
        scanner.close();
    }
    public static void main(String[] args) {
        setup();
        service();
    }
}
