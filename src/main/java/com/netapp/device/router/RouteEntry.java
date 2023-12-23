package com.netapp.device.router;

import com.netapp.device.Iface;

import java.util.Timer;
import java.util.TimerTask;

public class RouteEntry {
    /** 目标 IP 地址 */
    private String destinationAddress;

    /** 网关 IP 地址 */
    private String gatewayAddress;

    /** 子网掩码 */
    private String maskAddress;

    /** 路由器接口，通过该接口发送数据包以达到目标或网关 */
    private Iface iface;

    private RouteTable parent;
    private Timer timer;

    /**
     * 创建一个新的路由表条目。
     * @param destinationAddress 目标 IP 地址
     * @param gatewayAddress 网关 IP 地址
     * @param maskAddress 子网掩码
     * @param iface 通过该接口发送数据包以达到目标或网关
     */
    public RouteEntry(String destinationAddress, String gatewayAddress, String maskAddress, Iface iface) {
        this.destinationAddress = destinationAddress;
        this.gatewayAddress = gatewayAddress;
        this.maskAddress = maskAddress;
        this.iface = iface;
    }

    /**
     * @return 目标 IP 地址
     */
    public String getDestinationAddress() {
        return this.destinationAddress;
    }

    /**
     * @return 网关 IP 地址
     */
    public String getGatewayAddress() {
        return this.gatewayAddress;
    }

    public void setGatewayAddress(String gatewayAddress) {
        this.gatewayAddress = gatewayAddress;
    }

    /**
     * @return 子网掩码
     */
    public String getMaskAddress() {
        return this.maskAddress;
    }

    /**
     * @return 通过该接口发送数据包以达到目标或网关的路由器接口
     */
    public Iface getInterface() {
        return this.iface;
    }

    public RouteTable getParent() {
        return this.parent;
    }

    public void setParent(RouteTable parent) {
        this.parent = parent;
    }

    public void setInterface(Iface iface) {
        this.iface = iface;
    }

    public void start() {
        this.timer = new Timer();
        this.timer.schedule(new RemoveTiming(), 10000);
    }

    /**
     * 重新设置删除计时器，从30秒开始
     */
    public void reset() {
        this.timer.cancel();
        this.timer.purge();
        this.timer = new Timer();
        this.timer.schedule(new RemoveTiming(), 30000);
    }

    /**
     * 从父列表中删除自身
     */
    public void timedRemove() {
        parent.remove(this.getDestinationAddress(), this.getMaskAddress());
    }

    /**
     * 如果经过30秒，从父列表中删除自身
     */
    class RemoveTiming extends TimerTask {
        public void run() {
            timedRemove();
        }
    }

    public String toString() {
        return String.format("%s \t%s \t%s \t%s",
                this.destinationAddress,
                this.gatewayAddress,
                this.maskAddress,
                this.iface.getiName());
    }
}
