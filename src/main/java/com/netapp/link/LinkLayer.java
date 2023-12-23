package com.netapp.link;

import com.netapp.device.Device;
import com.netapp.device.Iface;
import com.netapp.device.NetIface;
import com.netapp.packet.Ethernet;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟链路层
 * */
public class LinkLayer  implements Runnable{

    private static List<Link> links;  // 链路集合
    private static List<Device> devices;  // 设备集合
    private static List<Iface> interfaces;  // 接口集合

    public LinkLayer() {
        links = new ArrayList<>();
        devices = new ArrayList<>();
        interfaces = new ArrayList<>();
    }

    public void addDevice(Device device){
        devices.add(device);
        interfaces.addAll(device.getInterfaces().values());
    }

    public void addLink(Iface i1, Iface i2){
        Link link = new Link(i1, i2);
        links.add(link);
    }

    private static List<Iface> findLkdIfaces(Iface i) {
        List<Iface> lkdIfaces = new ArrayList<>();
        for (Link link : links) {
            if (link.getI1().equals(i)) {
                lkdIfaces.add(link.getI2());
            }
            else if (link.getI2().equals(i)) {
                lkdIfaces.add(link.getI1());
            }
        }
        return lkdIfaces;
    }

    @Override
    public void run() {
        while(true){
            // 轮巡 interfaces
            for (Iface iface : interfaces) {
                Ethernet etherPacket = iface.getOutputPacket();
                if(etherPacket != null){
                    // 按 links 放入对应 inputQueue
                    List<Iface> lkdIfaces = findLkdIfaces(iface);
                    if(!lkdIfaces.isEmpty()){
                        if(etherPacket.isBroadcast()){ // 是广播以太网数据包
                            // 广播相连接口
                            for (Iface lkdIface : lkdIfaces){
                                lkdIface.receivePacket(etherPacket);
                            }
                        }else{
                            // 查找 MAC 匹配的接口
                            for (Iface lkdIface : lkdIfaces){
                                if( lkdIface instanceof NetIface ){
                                    // lkdIface 是网络层设备
                                    if(((NetIface)lkdIface).getMacAddress().equals(etherPacket.getDestinationMAC())){
                                        lkdIface.receivePacket(etherPacket);
                                    }
                                    else {
                                        System.out.println(
                                                "`````````````````LinkLayer: " +
                                                ((NetIface)lkdIface).getMacAddress() +
                                                " != " +
                                                etherPacket.getDestinationMAC()
                                        );
                                    }
                                }
                                else{
                                    // lkdIface 是链路层交换机
                                    lkdIface.receivePacket(etherPacket);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
