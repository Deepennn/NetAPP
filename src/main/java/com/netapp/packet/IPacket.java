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

    /**
     * 获取父级包。
     * @return 返回实现 IPacket 接口的对象，表示父级包。
     */
    public IPacket getParent();

    /**
     * 设置父级包。
     * @param packet 要设置为父级的包对象，必须实现 IPacket 接口。
     * @return 返回设置后的 IPacket 对象。
     */
    public IPacket setParent(IPacket packet);

    /**
     * 根据需要重置任何校验和，并在所有父包上调用 resetChecksum。
     */
    public void resetChecksum();

//    /**
//     * 设置所有有效负载的父包，然后将该包及所有有效负载序列化。
//     * @return 包含该包及其有效负载的字节数组。
//     */
//    public byte[] serialize();
//
//    /**
//     * 反序列化该包及所有可能的有效负载。
//     * @param data 要反序列化的数据。
//     * @param offset 开始反序列化的偏移量。
//     * @param length 要反序列化的数据长度。
//     * @return 反序列化后的数据。
//     */
//    public IPacket deserialize(byte[] data, int offset, int length);
//
//    /**
//     * 克隆该包及其有效负载，但不包括其父包。
//     * @return 返回克隆后的对象。
//     */
//    public Object clone();

}
