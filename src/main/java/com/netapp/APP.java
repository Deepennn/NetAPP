package com.netapp;

import com.netapp.config.DeviceFactory;
import com.netapp.net.Net;

import java.util.Scanner;

import static com.netapp.config.DeviceConfig.*;

public class APP {
    public static void main(String[] args) {

        /**----------------------------------SETUP-------------------------------------*/

        // 创建网络
        Net net = new Net(NET_NAME, DeviceFactory.provide());

        // 启动网络
        new Thread(net).start();

        /**----------------------------------------------------------------------------*/

        /**----------------------------------SEND-------------------------------------*/

        // 创建Scanner对象
        Scanner scanner = new Scanner(System.in);
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        System.out.println(
                "    WELCOME TO NETLAYER!\n" +
                "    YOU ARE NOW `h1` IN THE NETWORK TOPO AS BELOW:\n"
                );
        System.out.println(
                "    ```````````````````NETWORK TOPO`````````````````\n" +
                "    h1   <===>         r1        <===> h2\n" +
                "    h1_i <---> [ r1_i1 , r1_i2 ] <---> h2_i\n" +
                "    ^                                      \n" +
                "   ```````````````````````````````````````````````````"
                );
        System.out.print(
                "    TRY SEND A MESSAGE TO `h2`!\n" +
                "    YOUR(`h1`) MESSAGE: "
        );
        // 读取用户输入的 MESSAGE
        String message = scanner.nextLine();
        System.out.println("/**```````````````````YOUR MESSAGE IS SENDING!````````````````*/");




        // h1 -> h2               @TEST: h1 -> r1 ->h2
//        net.service.sendIPPacket(H1_HOSTNAME, H2_I_IP, message, 64);



        // h2 -> h1               @TEST: h2 -> r1 ->h1
        net.service.sendIPPacket(H2_HOSTNAME, H1_I_IP, message, 64);



        // h1 -> r1               @TEST: h1 -> r1
//        net.service.sendIPPacket(H1_HOSTNAME, R1_I1_IP, message, 64);



        // h1 -> ?                @TEST: ICMP_DESTINATION_NETWORK_UNREACHABLE
//        net.service.sendIPPacket(H1_HOSTNAME,"1.1.1.1", message, 64);



        // h1 -> h2               @TEST: ICMP_DESTINATION_TIME_EXCEEDED
//        net.service.sendIPPacket(H1_HOSTNAME, H2_I_IP, message, 1);



         // h1 ?-> h2              @TEST: ARP_REQ
//        By DELETING THE ENTRY `1.0.0.1 00:11:22:33:44:55`
//                 IN THE FILE `src/main/resources/config/arp_cache/h1.ac`


        // 关闭Scanner
        scanner.close();
        /**----------------------------------------------------------------------------*/

    }
}
