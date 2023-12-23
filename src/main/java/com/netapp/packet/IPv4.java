package com.netapp.packet;

import java.util.HashMap;
import java.util.Map;

public class IPv4 extends Packet {

    public static final String BROADCAST_IP = "255.255.255.255";

    public static final byte PROTOCOL_ICMP = 0x1;
    public static Map<Byte, Class<? extends IPacket>> protocolClassMap;

    static {
        protocolClassMap = new HashMap<Byte, Class<? extends IPacket>>();
        protocolClassMap.put(PROTOCOL_ICMP, ICMP.class);
    }

    private byte version;
    private String sourceIP;
    private String destinationIP;
    private int ttl;
    private byte protocol;
    private int checksum;

    /**
     * 默认构造函数，将版本设置为4。
     */
    public IPv4() {
        super();
        this.version = 4;
    }

    /**
     *  更新校验和
     */
    public void updateChecksum(){
        this.checksum = 0;
        this.setChecksum(this.calculateChecksum(this.toString()));
    }

    /**
     * @return 如果 IP 数据包是广播包，则返回true；否则返回false
     */
    public boolean isBroadcast() {
        return destinationIP.equals(BROADCAST_IP);
    }


    /**
     * 接受一个 IPv4 地址字符串，格式为 xxx.xxx.xxx.xxx，例如 192.168.0.1，
     * 并返回对应的 32 位整数。
     * @param ipAddress IPv4 地址字符串
     * @return 对应的 32 位整数
     */
    public static int toIPv4Address(String ipAddress) {
        if (ipAddress == null)
            throw new IllegalArgumentException("指定的 IPv4 地址必须包含由点分隔的 4 组数字");

        // 使用点（.）分割 IPv4 地址
        String[] octets = ipAddress.split("\\.");

        // 确保地址包含 4 组数字
        if (octets.length != 4)
            throw new IllegalArgumentException("指定的 IPv4 地址必须包含由点分隔的 4 组数字");

        int result = 0;
        // 将每个数字转换为整数，并根据其位置将其左移相应的位数后进行位或运算
        for (int i = 0; i < 4; ++i) {
            result |= Integer.valueOf(octets[i]) << ((3 - i) * 8);
        }
        return result;
    }

    /**
     * 接受一个 32 位整数表示的 IPv4 地址，返回形式为 xxx.xxx.xxx.xxx 的字符串。
     * 例如，192.168.0.1。
     *
     * @param ipAddress 32 位整数表示的 IPv4 地址
     * @return 形式为 xxx.xxx.xxx.xxx 的字符串
     */
    public static String fromIPv4Address(int ipAddress) {
        StringBuffer sb = new StringBuffer();
        int result = 0;

        // 将 32 位整数表示的 IPv4 地址拆分为 4 个字节，并使用点分隔
        for (int i = 0; i < 4; ++i) {
            result = (ipAddress >> ((3 - i) * 8)) & 0xff;
            sb.append(Integer.valueOf(result).toString());
            if (i != 3)
                sb.append(".");
        }
        return sb.toString();
    }

    public String getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    public String getDestinationIP() {
        return destinationIP;
    }

    public void setDestinationIP(String destinationIP) {
        this.destinationIP = destinationIP;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public byte getProtocol() {
        return protocol;
    }

    public void setProtocol(byte protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return "IPv4{" +
                "version=" + version +
                ", sourceIP='" + sourceIP + '\'' +
                ", destinationIP='" + destinationIP + '\'' +
                ", ttl=" + ttl +
                ", protocol=" + protocol +
                ", checksum=" + checksum +
                ", payload=" + payload +
                '}';
    }

}
