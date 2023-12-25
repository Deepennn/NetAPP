package com.netapp.device;

import com.netapp.packet.Ethernet;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Device implements Runnable
{
    /** 设备的主机名 */
    protected String hostname;

    /** 设备的接口列表；将接口名称映射到接口对象 */
    protected Map<String, Iface> interfaces;

    /**
     * 创建设备。
     * @param hostname 设备的主机名
     */
    public Device(String hostname)
    {
        this.hostname = hostname;
        this.interfaces = new HashMap<String, Iface>();
    }

    /**
     * 创建设备。
     * @param hostname 设备的主机名
     * @param interfaces 接口映射
     */
    public Device(String hostname, Map<String, Iface> interfaces) {
        this.hostname = hostname;
        this.interfaces = interfaces;
    }

    @Override
    public void run() {
        while (true) {
            interfaces.forEach((iName, iface) -> {
                if(iface.peekInputPacket() != null){
                    Ethernet etherPacket = iface.pollInputPacket();
                    handlePacket(etherPacket, iface);
                }
            });
        }

    }

    public Iface getDefaultInterface() {
        AtomicReference<Iface> defaultInterface = new AtomicReference<>();
        if (interfaces.size() == 1) {
            // 直接获取第一个元素
            defaultInterface.set(interfaces.values().iterator().next());
        } else {
            // Map 中不止一个元素或者是空的
            System.out.println("Default iface not found!");
        }
        return defaultInterface.get();
    }


    /**
     * 获取设备的主机名。
     * @return 设备的主机名
     */
    public String getHostname()
    { return this.hostname; }

    /**
     * 获取设备的接口列表。
     * @return 设备的接口列表；将接口名称映射到接口对象
     */
    public Map<String, Iface> getInterfaces()
    { return this.interfaces; }

    /**
     * 添加设备的一个接口。
     * @param ifaceName 接口的名称
     * @return 添加的接口对象
     */
    public Iface addInterface(String ifaceName)
    {
        Iface iface = new Iface(ifaceName);
        this.interfaces.put(ifaceName, iface);
        return iface;
    }

    /**
     * 根据接口名称获取设备上的接口。
     * @param ifaceName 所需接口的名称
     * @return 请求的接口；如果没有具有给定名称的接口，则为 null
     */
    public Iface getInterface(String ifaceName)
    { return this.interfaces.get(ifaceName); }

    /**
     * 发送以太网数据包到特定接口。
     * @param etherPacket 包含所有字段、封装头和有效载荷的以太网数据包
     * @param iface 要发送数据包的接口
     * @return 如果成功发送数据包，则为 true；否则为 false
     */
    public void sendPacket(Ethernet etherPacket, Iface iface)
    { iface.putOutputPacket(etherPacket); }

    /**
     * 处理接收到的以太网数据包的抽象方法。
     * @param etherPacket 接收到的以太网数据包
     * @param inIface 接收数据包的接口
     */
    public abstract void handlePacket(Ethernet etherPacket, Iface inIface);
}
