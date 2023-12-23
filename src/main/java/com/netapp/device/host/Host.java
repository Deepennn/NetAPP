package com.netapp.device.host;

import com.netapp.device.Iface;
import com.netapp.device.NetDevice;
import com.netapp.device.NetIface;
import com.netapp.device.router.ArpEntry;
import com.netapp.packet.*;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Host extends NetDevice {

    protected String gatewayAddress;   // 网关IP地址

    protected boolean isInSubnet(String dstIp, Iface outIface){

        int ip = IPv4.toIPv4Address(dstIp);

        // 使用接口的掩码对目标IP进行掩码操作
        int maskedDst = ip &
                IPv4.toIPv4Address(((NetIface)outIface).getSubnetMask());
        // 使用接口的掩码对主机IP进行掩码操作
        int hostSubnet = IPv4.toIPv4Address(((NetIface)outIface).getIpAddress()) &
                IPv4.toIPv4Address(((NetIface)outIface).getSubnetMask());

        // 返回掩码后的目标IP与子网地址匹配结果
        return maskedDst == hostSubnet;
    }

    /**
     * 创建主机。
     * @param hostname 设备的主机名
     * @param interfaces 接口映射
     */
    public Host(String hostname, Map<String, Iface> interfaces, String gatewayAddress) {
        super(hostname, interfaces);
        this.gatewayAddress = gatewayAddress;
    }

    /**
     * 处理 IP 数据包。
     * @param etherPacket 接收到的以太网数据包
     * @param inIface 接收数据包的接口
     */
    @Override
    protected void handleIPPacket(Ethernet etherPacket, Iface inIface) {
        if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4) {
            return;
        }

        IPv4 ipPacket = (IPv4) etherPacket.getPayload();
        System.out.println(this.hostname + " is handling IP packet:" + ipPacket);

        //TODO: CHKSUM
//        int origCksum = ipPacket.getChecksum();
//        int calcCksum = ipPacket.calculateChecksum(ipPacket.toString());
//        if (origCksum != calcCksum) {
//            return;
//        }

        System.out.println(this.hostname + " accepted IP packet:" + ipPacket);
        System.out.println("/**----------------------------------------------------------------*/");

    }

    /**
     * 发送 IP 数据包。
     * @param message 模拟的IP数据包载荷
     */
    public void sendIPPacket(String dstIp, String message) {
        Ethernet ether = new Ethernet();
        IPv4 ip = new IPv4();
        Data data = new Data(message);
        ether.setPayload(ip);
        ip.setPayload(data);

        ether.setEtherType(Ethernet.TYPE_IPv4);

        byte d = 64;
        ip.setTtl(d);
        ip.setDestinationIP(dstIp);

        Iface outIface = this.getDefaultInterface();

        // 在 ICMP Echo 回应中：源 IP 是上一次请求的接收方主机的 IP 地址
        ip.setSourceIP(((NetIface)outIface).getIpAddress());

        System.out.println(this.hostname + " is sending IP packet:" + ip);

        ether.setSourceMAC(((NetIface)outIface).getMacAddress());

        String nextHop = null;
        // 判断属不属于自己所属子网：
        if (isInSubnet(dstIp, outIface))
        { // 如果属于自己所属子网则直接设置目的 MAC 为目的地 MAC
            nextHop = dstIp;
        }
        else
        { // 否则将其设置为网关的 MAC
            nextHop = this.gatewayAddress;
        }

        ArpEntry arpEntry = this.atomicCache.get().lookup(nextHop);
        if (null == arpEntry) {

            System.out.println(this.hostname + " can't find arp entry for: " + nextHop);
            sendARPPacket(ether, nextHop, outIface);
            return;

        } else
            ether.setDestinationMAC(arpEntry.getMac());

        this.sendPacket(ether, outIface);
    }

    /**
     * 发送 ICMP 数据包。（错误响应）
     * @param etherPacket 接收到的以太网数据包
     * @param inIface 接收数据包的接口
     * @param type ICMP 类型
     * @param code ICMP 代码
     * @param echo 是否回显
     */
    @Override
    protected void sendICMPPacket(Ethernet etherPacket, Iface inIface, int type, int code, boolean echo) {
        Ethernet ether = new Ethernet();
        IPv4 ip = new IPv4();
        ICMP icmp = new ICMP();
        Data data = new Data();
        ether.setPayload(ip);
        ip.setPayload(icmp);
        icmp.setPayload(data);

        ether.setEtherType(Ethernet.TYPE_IPv4);

        byte d = 64;
        ip.setTtl(d);
        ip.setProtocol(IPv4.PROTOCOL_ICMP);
        ip.setDestinationIP(((IPv4) (etherPacket.getPayload())).getSourceIP());


        icmp.setIcmpType((byte) type);
        icmp.setIcmpCode((byte) code);

        IPv4 ipPacket = (IPv4) etherPacket.getPayload();
        String dstIp = ipPacket.getSourceIP();

        Iface outIface = this.getDefaultInterface();

        // 在 ICMP Echo 回应中：源 IP 是上一次请求的接收方主机的 IP 地址
        ip.setSourceIP(echo ? ipPacket.getDestinationIP() : ((NetIface)inIface).getMacAddress());

        ether.setSourceMAC(((NetIface)inIface).getMacAddress());

        String nextHop = null;
        // 判断属不属于自己所属子网：
        if (isInSubnet(dstIp, outIface))
        { // 如果属于自己所属子网则直接设置目的 MAC 为目的地 MAC
            nextHop = dstIp;
        }
        else
        { // 否则将其设置为网关的 MAC
            nextHop = this.gatewayAddress;
        }

        ArpEntry arpEntry = this.atomicCache.get().lookup(nextHop);
        if (null == arpEntry) {

            System.out.println(this.hostname + " can't find arp entry for: " + nextHop);
            sendARPPacket(etherPacket, nextHop, outIface);

            return;
        } else
            ether.setDestinationMAC(arpEntry.getMac());

        System.out.println(this.hostname + " is sending ICMP packet:" + ether);
        this.sendPacket(ether, outIface);
    }

    public String getGatewayAddress() {
        return gatewayAddress;
    }

    public void setGatewayAddress(String gatewayAddress) {
        this.gatewayAddress = gatewayAddress;
    }

}
