package com.netapp.packet;

public class ARP extends Packet {

    public static short HW_TYPE_ETHERNET = 0x1;

    public static short PROTO_TYPE_IP = 0x800;

    public static final short OP_REQUEST = 0x1;
    public static final short OP_REPLY = 0x2;



    protected short hardwareType;
    protected short protocolType;
    protected short opCode;

    protected String senderHardwareAddress;
    protected String senderProtocolAddress;
    protected String targetHardwareAddress;
    protected String targetProtocolAddress;

    public ARP(){
        super();
    }

    public short getHardwareType() {
        return hardwareType;
    }

    public void setHardwareType(short hardwareType) {
        this.hardwareType = hardwareType;
    }

    public short getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(short protocolType) {
        this.protocolType = protocolType;
    }

    public short getOpCode() {
        return opCode;
    }

    public void setOpCode(short opCode) {
        this.opCode = opCode;
    }

    public String getSenderHardwareAddress() {
        return senderHardwareAddress;
    }

    public void setSenderHardwareAddress(String senderHardwareAddress) {
        this.senderHardwareAddress = senderHardwareAddress;
    }

    public String getSenderProtocolAddress() {
        return senderProtocolAddress;
    }

    public void setSenderProtocolAddress(String senderProtocolAddress) {
        this.senderProtocolAddress = senderProtocolAddress;
    }

    public String getTargetHardwareAddress() {
        return targetHardwareAddress;
    }

    public void setTargetHardwareAddress(String targetHardwareAddress) {
        this.targetHardwareAddress = targetHardwareAddress;
    }

    public String getTargetProtocolAddress() {
        return targetProtocolAddress;
    }

    public void setTargetProtocolAddress(String targetProtocolAddress) {
        this.targetProtocolAddress = targetProtocolAddress;
    }

    @Override
    public String toString() {
        return "ARP{" +
                "hardwareType=" + hardwareType +
                ", protocolType=" + protocolType +
                ", opCode=" + opCode +
                ", senderHardwareAddress='" + senderHardwareAddress + '\'' +
                ", senderProtocolAddress='" + senderProtocolAddress + '\'' +
                ", targetHardwareAddress='" + targetHardwareAddress + '\'' +
                ", targetProtocolAddress='" + targetProtocolAddress + '\'' +
                ", payload=" + payload +
                '}';
    }

}
