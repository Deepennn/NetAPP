package com.netapp.link;

import com.netapp.device.Device;
import com.netapp.device.Iface;
import com.netapp.device.NetIface;
import com.netapp.packet.Ethernet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模拟链路层
 * */
public class LinkLayer implements Runnable{

    /** 拓扑图 */
    private Topo topo;
    private List<Link> links;  // 链路集合
    private Map<String,Device> devices;  // 设备集合
    private Map<String,Iface> interfaces;  // 接口集合

    public LinkLayer() {
        this.links = new ArrayList<>();
        this.devices = new HashMap<String, Device>();
        this.interfaces = new HashMap<String, Iface>();
        this.topo = new Topo();
    }

    public void addDevice(String hostname, Device device){
        devices.put(hostname,device);
        interfaces.putAll(device.getInterfaces());
    }

    public void addLink(Iface i1, Iface i2){
        Link link = new Link(i1, i2);
        links.add(link);
    }

    private List<Iface> findLkdIfaces(Iface i) {
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
            for (Iface iface : interfaces.values()) {
                if(iface.peekOutputPacket() != null){
                    Ethernet etherPacket = iface.pollOutputPacket();
                    List<Iface> lkdIfaces = findLkdIfaces(iface); // 按 links 放入对应 inputQueue
                    if(!lkdIfaces.isEmpty()){
                        if(etherPacket.isBroadcast()){ // 是广播以太网数据包
                            // 广播相连接口
                            for (Iface lkdIface : lkdIfaces){
                                lkdIface.putInputPacket(etherPacket);
                            }
                        }else{
                            // 查找 MAC 匹配的接口
                            for (Iface lkdIface : lkdIfaces){
                                if( lkdIface instanceof NetIface ){
                                    // lkdIface 是网络层设备
                                    if(((NetIface)lkdIface).getMacAddress().equals(etherPacket.getDestinationMAC())){
                                        lkdIface.putInputPacket(etherPacket);
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
                                    lkdIface.putInputPacket(etherPacket);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 从文件加载网络拓扑图。
     * @param topoFile 包含拓扑图的文件名
     */
    public void loadTopo(String topoFile) {
        if (!topo.load(topoFile, this)) {
            System.err.println("Error setting up topo from file " + topoFile);
            System.exit(1);
        }

        System.out.println("Loaded static topo");
        System.out.println("-------------------------------------------------");
        for (Link link: links) {
            System.out.print(link + " ");
        }
        System.out.println("\n-------------------------------------------------");
    }

    /**
     * 根据接口名称获取接口。
     * @param ifaceName 所需接口的名称
     * @return 请求的接口；如果没有具有给定名称的接口，则为 null
     */
    public Iface getInterface(String ifaceName)
    { return this.interfaces.get(ifaceName); }

    @Override
    public String toString() {
        return "LinkLayer{" +
                "links=" + links +
                ", devices=" + devices +
                ", interfaces=" + interfaces +
                '}';
    }

    public class Topo{
        /**
         * 从文件中加载拓扑图填充拓扑图。
         * @param filename 包含静态路由表的文件的名称
         * @param linklayer 与拓扑图相关联的链路层
         * @return 如果成功加载拓扑图则返回 true，否则返回 false
         */
        public boolean load(String filename, LinkLayer linklayer) {
            // 打开文件
            BufferedReader reader;
            try {
                FileReader fileReader = new FileReader(filename);
                reader = new BufferedReader(fileReader);
            } catch (FileNotFoundException e) {
                System.err.println(e.toString());
                return false;
            }

            while (true) {
                // 从文件读取拓扑图项
                String line = null;
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    System.err.println(e.toString());
                    try {
                        reader.close();
                    } catch (IOException f) {
                    }
                    return false;
                }

                // 如果已经到达文件末尾，则停止
                if (null == line) {
                    break;
                }

                // 解析用于拓扑图项的字段
                String linkPattern = "(link)";
                String ifacePattern = "([a-zA-Z0-9_]+)";
                Pattern pattern = Pattern.compile(String.format(
                        "%s\\s+%s\\s+%s",
                        linkPattern, ifacePattern, ifacePattern));
                Matcher matcher = pattern.matcher(line);
                if (!matcher.matches() || matcher.groupCount() != 3) {
                    System.err.println("拓扑图文件中的无效条目");
                    try {
                        reader.close();
                    } catch (IOException f) {
                    }
                    return false;
                }

                String ifaceName1 = matcher.group(2).trim();
                Iface iface1 = getInterface(ifaceName1);
                if (null == iface1) {
                    System.err.println("加载拓扑图时出错，无效的接口 "
                            + matcher.group(2));
                    try {
                        reader.close();
                    } catch (IOException f) {
                    }
                    return false;
                }

                String ifaceName2 = matcher.group(3).trim();
                Iface iface2 = getInterface(ifaceName2);
                if (null == iface2) {
                    System.err.println("加载拓扑图时出错，无效的接口 "
                            + matcher.group(3));
                    try {
                        reader.close();
                    } catch (IOException f) {
                    }
                    return false;
                }

                // 将条目添加到拓扑图
                addLink(iface1,iface2);
            }

            // 关闭文件
            try {
                reader.close();
            } catch (IOException f) {
            }
            return true;
        }
    }

}
