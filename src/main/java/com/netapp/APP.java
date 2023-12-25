package com.netapp;

import com.netapp.device.NetIface;
import com.netapp.link.LinkLayer;
import com.netapp.device.Iface;
import com.netapp.device.host.Host;
import com.netapp.device.router.Router;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class APP {

    /**----------------------------------CONSTANTS-------------------------------------*/

    // NETWORK TOPO:
    // h1   <===>         r1        <===> h2
    // h1_i <---> [ r1_i1 , r1_i2 ] <---> h2_i

    // r1
    public static final String R1_HOSTNAME = "r1";
    public static final String R1_I1_INAME = "r1_i1";
    public static final String R1_I2_INAME = "r1_i2";
    public static final String R1_I1_IP = "1.0.0.1";
    public static final String R1_I2_IP = "2.0.0.1";
    public static final String R1_I1_MAC = "00:11:22:33:44:55";
    public static final String R1_I2_MAC = "11:22:33:44:55:00";
    public static final String R1_I1_MASK = "255.255.255.0";
    public static final String R1_I2_MASK = "255.255.255.0";

    // h1
    public static final String H1_HOSTNAME = "h1";
    public static final String H1_I_INAME = "h1_i";
    public static final String H1_I_IP = "1.0.0.2";
    public static final String H1_I_MAC = "AA:BB:CC:DD:EE:FF";
    public static final String H1_I_MASK = "255.255.255.0";

    // h1
    public static final String H2_HOSTNAME = "h2";
    public static final String H2_I_INAME = "h2_i";
    public static final String H2_I_IP = "2.0.0.2";
    public static final String H2_I_MAC = "BB:CC:DD:EE:FF:AA";
    public static final String H2_I_MASK = "255.255.255.0";

    // h2
    /**----------------------------------------------------------------------------*/

    public static void main(String[] args) {

        /**----------------------------------创建线程-------------------------------------*/

        // 创建链路层线程
        LinkLayer linkLayer = new LinkLayer();
        Thread lkLyrThread = new Thread(linkLayer);

        // 创建路由器线程
        NetIface r1_i1 = new NetIface(R1_I1_INAME,R1_I1_IP,R1_I1_MAC,R1_I1_MASK);
        NetIface r1_i2 = new NetIface(R1_I2_INAME,R1_I2_IP,R1_I2_MAC,R1_I2_MASK);
        Map<String,Iface> interfaces_r1 = new HashMap<>();
        interfaces_r1.put(R1_I1_INAME,r1_i1);
        interfaces_r1.put(R1_I2_INAME,r1_i2);
        Router r1 = new Router(R1_HOSTNAME, interfaces_r1);
        Thread r1Thread = new Thread(r1);

        // 创建主机线程
        NetIface h1_i = new NetIface(H1_I_INAME,H1_I_IP,H1_I_MAC,H1_I_MASK);
        Map<String,Iface> interfaces_h1 = new HashMap<>();
        interfaces_h1.put(H1_I_INAME,h1_i);
        Host h1 = new Host(H1_HOSTNAME, interfaces_h1, R1_I1_IP);

        NetIface h2_i = new NetIface(H2_I_INAME,H2_I_IP,H2_I_MAC,H2_I_MASK);
        Map<String,Iface> interfaces_h2 = new HashMap<>();
        interfaces_h2.put(H2_I_INAME,h2_i);
        Host h2 = new Host(H2_HOSTNAME, interfaces_h2, R1_I2_IP);

        Thread h1Thread = new Thread(h1);
        Thread h2Thread = new Thread(h2);

        /**----------------------------------------------------------------------------*/

        /**----------------------------------注册 / 配置-------------------------------------*/

        // 注册设备
        linkLayer.addDevice(r1);
        linkLayer.addDevice(h1);
        linkLayer.addDevice(h2);

        // 注册链路
        // h1_i <---> [ r1_i1 , r1_i2 ] <---> h2_i
        linkLayer.addLink(h1_i,r1_i1);
        linkLayer.addLink(r1_i2,h2_i);

        // 配置路由表
        // 格式: 目的网络、子网掩码（匹配精度）、接口
        r1.getRoutingTable().insert(H2_I_IP, null, R1_I2_MASK, r1_i2);
        r1.getRoutingTable().insert(H1_I_IP, null, R1_I1_MASK, r1_i1);

        // 配置Arp缓存
        // 格式: 目的IP、目的MAC
        // 如果一个主机向自己所在的子网外发数据包，那么 MAC 应该设定为网关的 MAC ，实际上网关的 MAC 是需要先 ARP 得到的
        h1.getAtomicCache().get().insert(R1_I1_IP, R1_I1_MAC);
        h1.getAtomicCache().get().insert(H1_I_IP, H1_I_MAC);

//        r1.getAtomicCache().get().insert(H2_I_IP, H2_I_MAC);
        r1.getAtomicCache().get().insert(H1_I_IP, H1_I_MAC);
        r1.getAtomicCache().get().insert(R1_I1_IP, R1_I1_MAC);
        r1.getAtomicCache().get().insert(R1_I2_IP, R1_I2_MAC);

        h2.getAtomicCache().get().insert(R1_I2_IP, R1_I2_MAC);
        h2.getAtomicCache().get().insert(H2_I_IP, H2_I_MAC);


        /**----------------------------------------------------------------------------*/

        /**----------------------------------启动线程-------------------------------------*/
        r1Thread.start();
        h1Thread.start();
        h2Thread.start();
        lkLyrThread.start();
        /**----------------------------------------------------------------------------*/

        /**----------------------------------模拟发包-------------------------------------*/

        // 创建Scanner对象
        Scanner scanner = new Scanner(System.in);
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
                "    YOUR(`h1`) MESSAGE:"
        );
        // 读取用户输入的 MESSAGE
        String message = scanner.nextLine();
        System.out.println("/**```````````````````YOUR MESSAGE IS SENDING!````````````````*/");
        // h1 -> h2
        h1.sendIPPacket(H2_I_IP,message);
        // h2 -> h1
//        h2.sendIPPacket(H1_I_IP,message);
        // h1 -> r1
//        h1.sendIPPacket(R1_I1_IP,message);

        // 关闭Scanner
        scanner.close();
        /**----------------------------------------------------------------------------*/

    }

}
