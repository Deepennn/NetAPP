package com.netapp.packet;

public class ICMP extends Packet {

    private byte icmpType;
    private byte icmpCode;
    private int checksum;

    public ICMP(){
        super();
    }

    public void resetChecksum() {
        // 在这里模拟更新校验和的操作
        checksum = calculateChecksum(this.toString());
        super.resetChecksum();
    }

    public int getIcmpType() {
        return icmpType;
    }

    public void setIcmpType(byte icmpType) {
        this.icmpType = icmpType;
    }

    public int getIcmpCode() {
        return icmpCode;
    }

    public void setIcmpCode(byte icmpCode) {
        this.icmpCode = icmpCode;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    @Override
    public String toString() {
        return "ICMP{" +
                "icmpType=" + icmpType +
                ", icmpCode=" + icmpCode +
                ", checksum=" + checksum +
                ", payload=" + payload +
                '}';
    }

}
