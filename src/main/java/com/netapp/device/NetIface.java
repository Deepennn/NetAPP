package com.netapp.device;

public class NetIface extends Iface {

    protected String ipAddress;   // IP地址
    protected String subnetMask;    // 所在子网子网掩码

    public NetIface(String name, String ipAddress, String macAddress, String subnetMask) {
        super(name, macAddress);
        this.ipAddress = ipAddress;
        this.subnetMask = subnetMask;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSubnetMask()
    { return this.subnetMask; }

    public void setSubnetMask(String subnetMask)
    { this.subnetMask = subnetMask; }

}
