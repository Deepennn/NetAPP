package com.netapp.packet;

/**
 * 测试数据
 * */
public class Data extends Packet {

    private String data;

    public Data(){
        super();
    }

    public Data(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Data{" +
                "data='" + data + '\'' +
                '}';
    }

}
