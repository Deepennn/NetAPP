package com.netapp.device;

import com.netapp.packet.Ethernet;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Iface
{
    protected String iName;            // 接口名称

    protected BlockingQueue<Ethernet> inputQueue;
    protected BlockingQueue<Ethernet> outputQueue;

    public Iface(String iName) {
        this.iName = iName;
        this.inputQueue = new LinkedBlockingQueue<>();
        this.outputQueue = new LinkedBlockingQueue<>();
    }

    // putters: 放入
    public void receivePacket(Ethernet etherPacket) {
        System.out.println(this.iName + " is receiving Ether packet: " + etherPacket);
        try {
            inputQueue.put(etherPacket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(Ethernet etherPacket) {
        System.out.println(this.iName + " is sending Ether packet: " + etherPacket);
        try {
            outputQueue.put(etherPacket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // takers: 取出
    public Ethernet getInputPacket() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Ethernet getOutputPacket() {
        return outputQueue.poll(); // 不阻塞
    }

    public String getiName()
    { return this.iName; }

    public void setiName(String iName) {
        this.iName = iName;
    }

    public BlockingQueue<Ethernet> getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(BlockingQueue<Ethernet> inputQueue) {
        this.inputQueue = inputQueue;
    }

    public BlockingQueue<Ethernet> getOutputQueue() {
        return outputQueue;
    }

    public void setOutputQueue(BlockingQueue<Ethernet> outputQueue) {
        this.outputQueue = outputQueue;
    }

}
