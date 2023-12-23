package com.netapp.device;

public class NetIface extends Iface {

    protected String ipAddress;   // IP地址
    protected String macAddress;  // MAC地址
    private String subnetMask;    // 所在子网子网掩码

    public NetIface(String name, String ipAddress, String macAddress, String subnetMask) {
        super(name);
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.subnetMask = subnetMask;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getSubnetMask()
    { return this.subnetMask; }

    public void setSubnetMask(String subnetMask)
    { this.subnetMask = subnetMask; }

}
