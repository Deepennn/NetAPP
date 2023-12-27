package com.netapp.device;

/**
 * ARP表中的条目，将IP地址映射到MAC地址。
 */
public class ArpEntry
{

    /** 对应于MAC地址的IP地址 */
    private String ip;

    /** 对应于IP地址的MAC地址 */
    private String mac;

    /** 映射创建时的时间 */
    private long timeAdded;

    /**
     * 创建一个将IP地址映射到MAC地址的ARP表条目。
     * @param ip 对应于MAC地址的IP地址
     * @param mac 对应于IP地址的MAC地址
     */
    public ArpEntry(String ip, String mac)
    {
        this.ip = ip;
        this.mac = mac;
        this.timeAdded = System.currentTimeMillis();
    }

    /**
     * @return 对应于MAC地址的IP地址
     */
    public String getIp()
    { return this.ip; }

    /**
     * @return 对应于IP地址的MAC地址
     */
    public String getMac()
    { return this.mac; }


    /**
     * @return 映射创建时的时间（自纪元以来的毫秒数）
     */
    public long getTimeAdded()
    { return this.timeAdded; }

    public String toString()
    {
        return String.format("%s \t%s", this.ip,
                this.mac);
    }
}

