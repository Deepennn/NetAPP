package com.netapp.packet;

public class ICMP extends Packet {

    private byte icmpType;
    private byte icmpCode;
    private int checksum;

    public static final String ECHO_REQUEST = "ECHO_REQUEST";
    public static final String ECHO_REPLY = "ECHO_REPLY";
    public static final String DESTINATION_NETWORK_UNREACHABLE = "DESTINATION_NETWORK_UNREACHABLE";
    public static final String DESTINATION_HOST_UNREACHABLE = "DESTINATION_HOST_UNREACHABLE";
    public static final String TIME_EXCEEDED = "TIME_EXCEEDED";

    public ICMP(){
        super();
    }

    /**
     *  更新校验和
     */
    public void updateChecksum(){
        this.checksum = 0;
        this.setChecksum(this.calculateChecksum(this.toString()));
    }


    /**
     * 根据给定的类型和代码获取相应的消息。
     *
     * @param type 类型
     * @param code 代码
     * @return 相应的消息，如果未找到则返回null
     */
    public static String getMessage(int type, int code) {
        switch (type) {
            case 8:
                return (code == 0) ? ECHO_REQUEST : null;
            case 0:
                return (code == 0) ? ECHO_REPLY : null;
            case 3:
                return (code == 0) ? DESTINATION_NETWORK_UNREACHABLE :
                        (code == 1) ? DESTINATION_HOST_UNREACHABLE : null;
            case 11:
                return (code == 0) ? TIME_EXCEEDED : null;
            default:
                return null;
        }
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
