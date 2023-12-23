package com.netapp.packet;

import java.util.HashMap;
import java.util.Map;

public class Ethernet extends Packet {

    public static final String BROADCAST_MAC = "FF:FF:FF:FF:FF:FF";

    public static final short TYPE_ARP = 0x0806;
    public static final short TYPE_IPv4 = 0x0800;
    public static Map<Short, Class<? extends IPacket>> etherTypeClassMap;

    static {
        etherTypeClassMap = new HashMap<Short, Class<? extends IPacket>>();
        etherTypeClassMap.put(TYPE_ARP, ARP.class);
        etherTypeClassMap.put(TYPE_IPv4, IPv4.class);
    }

    private String sourceMAC;
    private String destinationMAC;
    private short etherType;
    private int checksum;

    public Ethernet(){
        super();
    }

    @Override
    public void resetChecksum() {
        // 在这里模拟更新校验和的操作
        checksum = calculateChecksum(this.toString());
    }

    /**
     * @return 如果以太帧是广播帧，则返回true；否则返回false
     */
    public boolean isBroadcast() {
        return destinationMAC.equals(BROADCAST_MAC);
    }

    public String getSourceMAC() {
        return sourceMAC;
    }

    public String getDestinationMAC() {
        return destinationMAC;
    }

    public void setSourceMAC(String sourceMAC) {
        this.sourceMAC = sourceMAC;
    }

    public void setDestinationMAC(String destinationMAC) {
        this.destinationMAC = destinationMAC;
    }

    public int getEtherType() {
        return etherType;
    }

    public void setEtherType(short etherType) {
        this.etherType = etherType;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    @Override
    public String toString() {
        return "Ethernet{" +
                "sourceMAC='" + sourceMAC + '\'' +
                ", destinationMAC='" + destinationMAC + '\'' +
                ", etherType=" + etherType +
                ", checksum=" + checksum +
                ", payload=" + payload +
                '}';
    }
}
