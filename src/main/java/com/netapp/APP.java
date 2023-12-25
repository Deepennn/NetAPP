package com.netapp;

import com.netapp.device.NetIface;
import com.netapp.link.LinkLayer;
import com.netapp.device.Iface;
import com.netapp.device.host.Host;
import com.netapp.device.router.Router;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static com.netapp.config.Constant.*;

public class APP {
    public static void main(String[] args) {
        /**----------------------------------注册 / 配置-------------------------------------*/
        // 创建链路层
        LinkLayer linkLayer = new LinkLayer();

        // 创建路由器
        NetIface r1_i1 = new NetIface(R1_I1_INAME,R1_I1_IP,R1_I1_MAC,R1_I1_MASK);
        NetIface r1_i2 = new NetIface(R1_I2_INAME,R1_I2_IP,R1_I2_MAC,R1_I2_MASK);
        Map<String,Iface> interfaces_r1 = new HashMap<>();
        interfaces_r1.put(R1_I1_INAME,r1_i1);
        interfaces_r1.put(R1_I2_INAME,r1_i2);
        Router r1 = new Router(R1_HOSTNAME, interfaces_r1);

        // 创建主机
        NetIface h1_i = new NetIface(H1_I_INAME,H1_I_IP,H1_I_MAC,H1_I_MASK);
        Map<String,Iface> interfaces_h1 = new HashMap<>();
        interfaces_h1.put(H1_I_INAME,h1_i);
        Host h1 = new Host(H1_HOSTNAME, interfaces_h1, R1_I1_IP);

        NetIface h2_i = new NetIface(H2_I_INAME,H2_I_IP,H2_I_MAC,H2_I_MASK);
        Map<String,Iface> interfaces_h2 = new HashMap<>();
        interfaces_h2.put(H2_I_INAME,h2_i);
        Host h2 = new Host(H2_HOSTNAME, interfaces_h2, R1_I2_IP);

        // 在链路层注册设备
        linkLayer.addDevice(r1.getHostname(),r1);
        linkLayer.addDevice(h1.getHostname(),h1);
        linkLayer.addDevice(h2.getHostname(),h2);

        /*
        // 在链路层注册链路
        // h1_i <---> [ r1_i1 , r1_i2 ] <---> h2_i
        linkLayer.addLink(h1_i,r1_i1);
        linkLayer.addLink(r1_i2,h2_i);

         */
        linkLayer.loadTopo(TOPO_FILENAME);

        /*
        // 配置路由表
        // 格式: 目的网络、子网掩码（匹配精度）、接口
        r1.getRoutingTable().insert(H2_I_IP, IPv4.DEFAULT_IP, R1_I2_MASK, r1_i2);
        r1.getRoutingTable().insert(H1_I_IP, IPv4.DEFAULT_IP, R1_I1_MASK, r1_i1);

         */

        /*
        // 配置Arp缓存
        // 格式: 目的IP、目的MAC
        // 如果一个主机向自己所在的子网外发数据包，那么 MAC 应该设定为网关的 MAC ，实际上网关的 MAC 是需要先 ARP 得到的
        h1.getAtomicCache().get().insert(R1_I1_IP, R1_I1_MAC);
        h1.getAtomicCache().get().insert(H1_I_IP, H1_I_MAC);

        //                                                         @TEST: ARP_REQ
        r1.getAtomicCache().get().insert(H2_I_IP, H2_I_MAC);
        r1.getAtomicCache().get().insert(H1_I_IP, H1_I_MAC);
        r1.getAtomicCache().get().insert(R1_I1_IP, R1_I1_MAC);
        r1.getAtomicCache().get().insert(R1_I2_IP, R1_I2_MAC);

        h2.getAtomicCache().get().insert(R1_I2_IP, R1_I2_MAC);
        h2.getAtomicCache().get().insert(H2_I_IP, H2_I_MAC);

         */

        /**----------------------------------------------------------------------------*/

        /**----------------------------------创建、启动线程-------------------------------------*/
        Thread lkLyrThread = new Thread(linkLayer);
        Thread r1Thread = new Thread(r1);
        Thread h1Thread = new Thread(h1);
        Thread h2Thread = new Thread(h2);
        r1Thread.start();
        h1Thread.start();
        h2Thread.start();
        lkLyrThread.start(); // 链路层最后启动
        /**----------------------------------------------------------------------------*/

        /**----------------------------------模拟发包-------------------------------------*/

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
        h1.sendIPPacket(H2_I_IP, message, 64);



        // h2 -> h1               @TEST: h2 -> r1 ->h1
//        h2.sendIPPacket(H1_I_IP, message, 64);



        // h1 -> r1               @TEST: h1 -> r1
//        h1.sendIPPacket(R1_I1_IP, message, 64);



        // h1 -> ?                @TEST: ICMP_DESTINATION_NETWORK_UNREACHABLE
//        h1.sendIPPacket("1.1.1.1", message, 64);



        // h1 -> h2               @TEST: ICMP_DESTINATION_TIME_EXCEEDED
//        h1.sendIPPacket(H2_I_IP, message, 1);



         // h1 ?-> h2              @TEST: ARP_REQ
//        By DELETING THE ENTRY `1.0.0.1 00:11:22:33:44:55`
//                 IN THE FILE `src/main/resources/config/arp_cache/h1.ac`


        // 关闭Scanner
        scanner.close();
        /**----------------------------------------------------------------------------*/

    }
}
