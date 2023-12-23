package com.netapp.packet;

public interface IPacket {
    /**
     * 获取有效负载。
     * @return 返回实现 IPacket 接口的对象，表示有效负载。
     */
    public IPacket getPayload();

    /**
     * 设置有效负载。
     * @param packet 要设置的有效负载对象，必须实现 IPacket 接口。
     * @return 返回设置后的 IPacket 对象。
     */
    public IPacket setPayload(IPacket packet);

}
