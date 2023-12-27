package com.netapp.packet;

public abstract class Packet implements IPacket{

    protected IPacket payload;

    /**
     * 获取有效负载。
     * @return 返回实现 IPacket 接口的对象，表示有效负载。
     */
    @Override
    public IPacket getPayload() {
        return payload;
    }

    /**
     * 设置有效负载。
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
     *
     *    实际实现：
     *    在发送数据时，为了计算数据包的校验和。应该按如下步骤：
     * 　　（1）把校验和字段置为0；
     * 　　（2）把需校验的数据看成以16位为单位的数字组成，依次进行二进制反码求和；
     * 　　（3）把得到的结果存入校验和字段中。
     * 　　在接收数据时，计算数据包的校验和相对简单，按如下步骤：
     * 　　（1）把首部看成以16位为单位的数字组成，依次进行二进制反码求和，包括校验和字段；
     * 　　（2）检查计算出的校验和的结果是否为0；
     * 　　（3）如果等于0，说明被整除，校验是和正确。否则，校验和就是错误的，协议栈要抛弃这个数据包。
     *
     *     我的实现：
     *     在发送数据时：
     * 　　（1）把校验和字段置为0；
     * 　　（2）求和；
     * 　　（3）把得到的结果存入校验和字段中。
     * 　　在接收数据时：
     * 　　（1）求和；
     * 　　（2）检查计算出的校验和的结果是否与之前一致；
     * 　　（3）如果等于0，说明被整除，校验是和正确。否则，校验和就是错误的，协议栈要抛弃这个数据包。
     */
    public int calculateChecksum(String data) {
        int checksum = 0;
        for (char c : data.toCharArray()) {
            checksum += (int) c;
        }
        return checksum;
    }

}
