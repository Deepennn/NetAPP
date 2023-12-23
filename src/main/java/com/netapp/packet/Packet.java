package com.netapp.packet;

public abstract class Packet implements IPacket{

    protected IPacket payload;

    /**
     * 获取有效负载（payload）。
     * @return 返回实现 IPacket 接口的对象，表示有效负载。
     */
    @Override
    public IPacket getPayload() {
        return payload;
    }

    /**
     * 设置有效负载（payload）。
     * @param payload 要设置的有效负载对象，必须实现 IPacket 接口。
     * @return 返回设置后的 IPacket 对象。
     */
    @Override
    public IPacket setPayload(IPacket payload) {
        this.payload = payload;
        return this;
    }

    /**
     *  简单的模拟累加校验和的计算
     */
    public int calculateChecksum(String data) {
        int checksum = 0;
        for (char c : data.toCharArray()) {
            checksum += (int) c;
        }
        return checksum;
    }

}
