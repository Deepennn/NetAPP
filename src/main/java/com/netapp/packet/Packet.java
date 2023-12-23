package com.netapp.packet;

public abstract class Packet implements IPacket{
    protected IPacket parent;
    protected IPacket payload;

    public int calculateChecksum(String data) {
        // 简单的模拟累加校验和的计算
        int checksum = 0;
        for (char c : data.toCharArray()) {
            checksum += (int) c;
        }
        return checksum;
    }

    /**
     * 获取父包。
     * @return 返回实现 IPacket 接口的对象，表示父级包。
     */
    @Override
    public IPacket getParent() {
        return parent;
    }

    /**
     * 设置父包。
     * @param parent 要设置为父级的包对象，必须实现 IPacket 接口。
     * @return 返回设置后的 IPacket 对象。
     */
    @Override
    public IPacket setParent(IPacket parent) {
        this.parent = parent;
        return this;
    }

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
     * 根据需要重置任何校验和，并在所有父包上调用 resetChecksum。
     */
    @Override
    public void resetChecksum() {
        if (this.parent != null)
            this.parent.resetChecksum();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 6733;
        int result = 1;
        result = prime * result + ((payload == null) ? 0 : payload.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Packet))
            return false;
        Packet other = (Packet) obj;
        if (payload == null) {
            if (other.payload != null)
                return false;
        } else if (!payload.equals(other.payload))
            return false;
        return true;
    }

//    @Override
//    public Object clone() {
//        IPacket pkt;
//        try {
//            // 创建新的包对象
//            pkt = this.getClass().newInstance();
//        } catch (Exception e) {
//            throw new RuntimeException("Could not clone packet");
//        }
//        // 使用 serialize() / deserialize() 来执行克隆
//        byte[] data = this.serialize();
//        pkt.deserialize(this.serialize(), 0, data.length);
//        pkt.setParent(this.parent);
//        return pkt;
//    }

}
