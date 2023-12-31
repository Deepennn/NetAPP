package com.netapp.device;

import com.netapp.packet.ARP;
import com.netapp.packet.Ethernet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import static com.netapp.config.DeviceConfig.ARP_CACHE_PREFFIX;
import static com.netapp.config.DeviceConfig.ARP_CACHE_SUFFIX;

public abstract class NetDevice extends Device
{

    /** ARP 缓存 */
    protected AtomicReference<ArpCache> atomicCache;

    /** 为ARP设置的输出缓存区 （不知道目的 MAC 的目的 IP） --> （对应数据包队列）  */
    protected HashMap<String, BlockingQueue<Ethernet>> outputQueueMap;

    /**
     * 创建设备。
     * @param hostname 设备的主机名
     * @param interfaces 接口映射
     */
    public NetDevice(String hostname, Map<String, Iface> interfaces)
    {
        super(hostname, interfaces);
        this.atomicCache = new AtomicReference<>(new ArpCache());
        this.outputQueueMap = new HashMap<>();
        this.loadArpCache(ARP_CACHE_PREFFIX + this.hostname + ARP_CACHE_SUFFIX);
    }

    /**
     * 处理在特定接口接收到的以太网数据包。
     * @param etherPacket 接收到的以太网数据包
     * @param inIface 接收数据包的接口
     */
    public void handlePacket(Ethernet etherPacket, Iface inIface) {
        System.out.println(this.hostname + " is receiving Ether packet: " + etherPacket.toString());

        /********************************************************************/

        /* 检验 MAC                                               */
        if(!inIface.getMacAddress().equals(etherPacket.getDestinationMAC()) &&
                !etherPacket.isBroadcast()){
            return;
        }

        /* 检验校验和                                               */
        int origCksum = etherPacket.getChecksum();
        etherPacket.updateChecksum();
        int calcCksum = etherPacket.getChecksum();
        if (origCksum != calcCksum) {
            System.out.println(this.hostname + " found Ether packet's checksum is wrong: ");
            return;
        }
        /* 处理数据包                                               */
        switch (etherPacket.getEtherType()) {
            case Ethernet.TYPE_IPv4:
                this.handleIPPacket(etherPacket, inIface);
                break;
            case Ethernet.TYPE_ARP:
                this.handleARPPacket(etherPacket, inIface);
                break;
            // 暂时忽略其他数据包类型
        }

        /********************************************************************/
    }


    /**
     * 处理 IP 数据包。
     * @param etherPacket 接收到的以太网数据包
     * @param inIface 接收数据包的接口
     */
    protected abstract void handleIPPacket(Ethernet etherPacket, Iface inIface);


    /**
     * 发送 ICMP 数据包。（错误响应）
     * @param etherPacket 接收到的以太网数据包
     * @param inIface 接收数据包的接口
     * @param type ICMP 类型
     * @param code ICMP 代码
     * @param echo 是否回显
     */
    protected abstract void sendICMPPacket(Ethernet etherPacket, Iface inIface, int type, int code, boolean echo);


    /**
     * 处理 ARP 数据包。
     * @param etherPacket 接收到的以太网数据包
     * @param inIface 接收数据包的接口
     */
    protected void handleARPPacket(Ethernet etherPacket, Iface inIface) {
        if (etherPacket.getEtherType() != Ethernet.TYPE_ARP) {
            return;
        }

        ARP arpPacket = (ARP) etherPacket.getPayload();
        System.out.println(this.hostname + " is handling ARP packet: " + arpPacket);

        if (arpPacket.getOpCode() != ARP.OP_REQUEST) {
            if (arpPacket.getOpCode() == ARP.OP_REPLY) { // 收到的是 ARP 响应数据包

                // 放入 ARP 缓存
                String srcIp = arpPacket.getSenderProtocolAddress();
                atomicCache.get().insert(srcIp, arpPacket.getSenderHardwareAddress());

                Queue<Ethernet> packetsToSend = outputQueueMap.get(srcIp); // outputQueueMap 中目的 IP 是响应源 IP 的数据包队列
                while(packetsToSend != null && packetsToSend.peek() != null){
                    Ethernet packet = packetsToSend.poll();
                    packet.setDestinationMAC(arpPacket.getSenderHardwareAddress());
                    packet.updateChecksum();
                    this.sendPacket(packet, inIface);
                }
            }
            return;
        }

        // ARP 请求数据包

        String targetIp = arpPacket.getTargetProtocolAddress();
        if (!Objects.equals(targetIp, ((NetIface) inIface).getIpAddress())) // 不是对应接口 IP 则不处理
            return;

        Ethernet ether = new Ethernet();
        ether.setEtherType(Ethernet.TYPE_ARP);
        ether.setSourceMAC(inIface.getMacAddress());
        ether.setDestinationMAC(etherPacket.getSourceMAC());

        ARP arp = new ARP();
        arp.setHardwareType(ARP.HW_TYPE_ETHERNET);
        arp.setProtocolType(ARP.PROTO_TYPE_IP);
        arp.setOpCode(ARP.OP_REPLY);
        arp.setSenderHardwareAddress(inIface.getMacAddress());
        arp.setSenderProtocolAddress(((NetIface)inIface).getIpAddress());
        arp.setTargetHardwareAddress(arpPacket.getSenderHardwareAddress());
        arp.setTargetProtocolAddress(arpPacket.getSenderProtocolAddress());

        ether.setPayload(arp);

        System.out.println(this.hostname + " is sending ARP packet:" + ether);

        ether.updateChecksum();
        this.sendPacket(ether, inIface);
        return;
    }

    /**
     * 发送 ARP 数据包。
     * @param etherPacket 接收到的以太网数据包
     * @param dstIp ICMP 类型
     * @param outIface 发送数据包的接口
     */
    protected void sendARPPacket(Ethernet etherPacket, String dstIp, Iface outIface){
        ARP arp = new ARP();
        arp.setHardwareType(ARP.HW_TYPE_ETHERNET);
        arp.setProtocolType(ARP.PROTO_TYPE_IP);
        arp.setOpCode(ARP.OP_REQUEST);
        arp.setSenderHardwareAddress(outIface.getMacAddress());
        arp.setSenderProtocolAddress(((NetIface)outIface).getIpAddress());
        arp.setTargetHardwareAddress(null);
        arp.setTargetProtocolAddress(dstIp);

        final AtomicReference<Ethernet> atomicEtherPacket = new AtomicReference<>(new Ethernet());
        final AtomicReference<Iface> atomicIface = new AtomicReference<>(outIface);
        final AtomicReference<Ethernet> atomicInPacket = new AtomicReference<>(etherPacket);

        atomicEtherPacket.get().setEtherType(Ethernet.TYPE_ARP);
        atomicEtherPacket.get().setSourceMAC(outIface.getMacAddress());

        atomicEtherPacket.get().setPayload(arp);
        atomicEtherPacket.get().setDestinationMAC(Ethernet.BROADCAST_MAC); // 广播 ARP 请求数据包
        atomicEtherPacket.get().updateChecksum();


        if (!outputQueueMap.containsKey(dstIp)) {
            outputQueueMap.put(dstIp, new LinkedBlockingQueue<>());
            System.out.println(hostname + " is making a new buffer queue for: " + dstIp);
        }
        BlockingQueue<Ethernet> nextHopQueue = outputQueueMap.get(dstIp);

        // 放入（不阻塞）
        try {
            nextHopQueue.put(etherPacket);
        } catch (InterruptedException e) {
//            System.out.println(this.hostname + " blocked a sending Ether packet: " + etherPacket);
            e.printStackTrace();
        }

        final AtomicReference<BlockingQueue<Ethernet>> atomicQueue = new AtomicReference<BlockingQueue<Ethernet>>(nextHopQueue); // 线程安全

        Thread waitForReply = new Thread(new Runnable() {

            public void run() {

                try {
                    System.out.println(hostname + " is sending ARP packet:" + atomicEtherPacket.get());
                    sendPacket(atomicEtherPacket.get(), atomicIface.get());
                    Thread.sleep(1000);
                    if (atomicCache.get().lookup(dstIp) != null) {
                        System.out.println(hostname + ": Found it: " + atomicCache.get().lookup(dstIp));
                        return;
                    }
                    System.out.println(hostname + " is sending ARP packet:" + atomicEtherPacket.get());
                    sendPacket(atomicEtherPacket.get(), atomicIface.get());
                    Thread.sleep(1000);
                    if (atomicCache.get().lookup(dstIp) != null) {
                        System.out.println(hostname + ": Found it: " + atomicCache.get().lookup(dstIp));
                        return;
                    }
                    System.out.println(hostname + " is sending ARP packet:" + atomicEtherPacket.get());
                    sendPacket(atomicEtherPacket.get(), atomicIface.get());
                    Thread.sleep(1000);
                    if (atomicCache.get().lookup(dstIp) != null) {
                        System.out.println(hostname + ": Found it: " + atomicCache.get().lookup(dstIp));
                        return;
                    }

                    // 都发了 3 次了，实在是真的是找不着 MAC，那就放弃吧，发送一个`目的主机不可达`的 ICMP
                    System.out.println(hostname + ": Not found: " + dstIp);

                    while (atomicQueue.get() != null && atomicQueue.get().peek() != null) {
                        atomicQueue.get().poll();
                    }
                    sendICMPPacket(atomicInPacket.get(), atomicIface.get(), 3, 1, false);
                    return;
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        });
        waitForReply.start();
        return;
    }


    /**
     * 从文件加载 ARP 缓存。
     * @param arpCacheFile 包含 ARP 缓存的文件名
     */
    public void loadArpCache(String arpCacheFile) {
        if (!atomicCache.get().load(arpCacheFile)) {
            System.err.println("Error setting up ARP cache from file " + arpCacheFile);
            System.exit(1);
        }

        System.out.println(this.hostname + " loaded static ARP cache");
        System.out.println("----------------------------------");
        System.out.print(this.atomicCache.get().toString());
        System.out.println("----------------------------------");
    }

    public  AtomicReference<ArpCache> getAtomicCache() {
        return atomicCache;
    }

    public void setAtomicCache(AtomicReference<ArpCache> atomicCache) {
        this.atomicCache = atomicCache;
    }

}
