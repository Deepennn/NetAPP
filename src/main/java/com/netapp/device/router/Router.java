package com.netapp.device.router;

import com.netapp.device.Iface;
import com.netapp.device.NetDevice;
import com.netapp.device.NetIface;
import com.netapp.packet.*;

import java.util.Map;
import java.util.Objects;

import static com.netapp.config.DeviceConfig.ROUTE_TABLE_PREFFIX;
import static com.netapp.config.DeviceConfig.ROUTE_TABLE_SUFFIX;

public class Router extends NetDevice {

    /** 路由表 */
    private RouteTable routeTable;

    /**
     * 创建路由器。
     * @param hostname 设备的主机名
     * @param interfaces 接口映射
     */
    public Router(String hostname, Map<String, Iface> interfaces) {
        super(hostname, interfaces);
        routeTable = new RouteTable();
        this.loadRouteTable(ROUTE_TABLE_PREFFIX + this.hostname + ROUTE_TABLE_SUFFIX);
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
        System.out.println(this.hostname + " is handling IP packet: " + ipPacket);

        // 检验校验和
        int origCksum = ipPacket.getChecksum();
        ipPacket.updateChecksum();
        int calcCksum = ipPacket.getChecksum();
        if (origCksum != calcCksum) {
            return;
        }

        // TTL-1
        ipPacket.setTtl((ipPacket.getTtl() - 1));
        if (0 == ipPacket.getTtl()) {
            this.sendICMPPacket(etherPacket, inIface, 11, 0, false);
            return;
        }

        // 更新校验和
        ipPacket.updateChecksum();

        // 检查数据包的目的 IP 是否为接口 IP 之一
        for (Iface iface : this.interfaces.values()) {
            if (Objects.equals(ipPacket.getDestinationIP(), ((NetIface) iface).getIpAddress())) {
                byte protocol = ipPacket.getProtocol();
//                System.out.println("ipPacket protocol: " + protocol);
                if (protocol == IPv4.PROTOCOL_ICMP) {
                    ICMP icmp = (ICMP) ipPacket.getPayload();
                    System.out.println(this.hostname + " accepted message: " + icmp);
                    if (icmp.getIcmpType() == 8) {
                        this.sendICMPPacket(etherPacket, inIface, 0, 0, true);
                    }
                }
                else if (protocol == IPv4.PROTOCOL_DEFAULT){
                    Data data = (Data) ipPacket.getPayload();
                    System.out.println(this.hostname + " accepted message: " + data.getData());
                }
                return;
            }
        }

        // 检查路由表并转发
        this.forwardIPPacket(etherPacket, inIface);
    }



    /**
     * 转发 IP 数据包。
     * @param etherPacket 接收到的以太网数据包
     * @param inIface 接收数据包的接口
     */
    private void forwardIPPacket(Ethernet etherPacket, Iface inIface) {
        // 确保是 IP 数据包
        if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4) {
            return;
        }
        IPv4 ipPacket = (IPv4) etherPacket.getPayload();
        System.out.println(this.hostname + " is forwarding IP packet: "  + ipPacket);

        // 获取目的 IP
        String dstIp = ipPacket.getDestinationIP();

        // 查找匹配的路由表项
        RouteEntry bestMatch = this.routeTable.lookup(dstIp);

        // 如果没有匹配的项，则什么也不做
        if (null == bestMatch) {
            this.sendICMPPacket(etherPacket, inIface, 3, 0, false);
            return;
        }

        // 确保不将数据包发送回它进入的接口
        NetIface outIface = (NetIface) bestMatch.getInterface();
        if (outIface == inIface) {
            return;
        }

        // 设置以太网头部中的源 MAC 地址
        String srcMac = outIface.getMacAddress();
        etherPacket.setSourceMAC(srcMac);

        // 如果没有网关，那么下一跳是 IP 目的地，设置目的 MAC 的时候可以直接设置目的地的MAC，否则设置网关的MAC
        String nextHop = bestMatch.getGatewayAddress();
        if (IPv4.DEFAULT_IP.equals(nextHop)) {
            nextHop = dstIp;
        }

        // 设置以太网头部中的目标 MAC 地址
        ArpEntry arpEntry = this.atomicCache.get().lookup(nextHop);
        if (null == arpEntry) {

            System.out.println(this.hostname + " can't find arp entry for: " + dstIp);
            sendARPPacket(etherPacket, nextHop, outIface);

            return;
        } else
            etherPacket.setDestinationMAC(arpEntry.getMac());

        this.sendPacket(etherPacket, outIface);
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
        Data data = new Data(ICMP.getMessage(type,code));
        ether.setPayload(ip);
        ip.setPayload(icmp);
        icmp.setPayload(data);

        // 更新校验和
        icmp.updateChecksum();

        ether.setEtherType(Ethernet.TYPE_IPv4);

        byte d = 64;
        ip.setTtl(d);
        ip.setProtocol(IPv4.PROTOCOL_ICMP);
        ip.setDestinationIP(((IPv4) (etherPacket.getPayload())).getSourceIP());

        icmp.setIcmpType((byte) type);
        icmp.setIcmpCode((byte) code);

        IPv4 ipPacket = (IPv4) etherPacket.getPayload();
        String dstIp = ipPacket.getSourceIP();
        RouteEntry bestMatch = this.routeTable.lookup(dstIp);
        if (null == bestMatch) {
            return;
        }
        Iface outIface = bestMatch.getInterface();

        // 在 ICMP Echo 回应中：源 IP 是上一次请求的接收方主机的 IP 地址
        ip.setSourceIP(echo ? ipPacket.getDestinationIP() : ((NetIface)inIface).getMacAddress());

        // 更新校验和
        ip.updateChecksum();

        System.out.println(this.hostname + " is sending ICMP packet:" + ether);

        // echo 是返回原子网
        ether.setSourceMAC(((NetIface)inIface).getMacAddress());

        String nextHop = bestMatch.getGatewayAddress();
        if (IPv4.DEFAULT_IP.equals(nextHop)) {
            nextHop = dstIp;
        }

        ArpEntry arpEntry = this.atomicCache.get().lookup(nextHop);
        if (null == arpEntry) {

            System.out.println(this.hostname + " can't find arp entry for: " + dstIp);
            sendARPPacket(etherPacket, nextHop, outIface);

            return;
        } else
            ether.setDestinationMAC(arpEntry.getMac());

        this.sendPacket(ether, outIface);
    }

    public RouteTable getRoutingTable() {
        return routeTable;
    }

    public void setRoutingTable(RouteTable routeTable) {
        this.routeTable = routeTable;
    }

    /**
     * 从文件加载路由表。
     * @param routeTableFile 包含路由表的文件名
     */
    public void loadRouteTable(String routeTableFile) {
        if (!routeTable.load(routeTableFile, this)) {
            System.err.println("Error setting up routing table from file " + routeTableFile);
            System.exit(1);
        }

        System.out.println(this.hostname + " loaded static route table");
        System.out.println("-------------------------------------------------");
        System.out.print(this.routeTable.toString());
        System.out.println("-------------------------------------------------");
    }

}
