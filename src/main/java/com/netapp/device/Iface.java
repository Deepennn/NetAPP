package com.netapp.device;

import com.netapp.packet.Ethernet;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Iface
{
    protected String iName;            // 接口名称

    protected BlockingQueue<Ethernet> inputQueue; // 输入队列
    protected BlockingQueue<Ethernet> outputQueue; // 输出队列

    public Iface(String iName) {
        this.iName = iName;
        this.inputQueue = new LinkedBlockingQueue<>();
        this.outputQueue = new LinkedBlockingQueue<>();
    }

    /**---------------------------------putters:放入（阻塞）-------------------------------------*/

    // 由 LinkLayer 放入数据包
    public void putInputPacket(Ethernet etherPacket) {
        System.out.println(this.iName + " is receiving Ether packet: " + etherPacket);
        try {
            inputQueue.put(etherPacket);
        } catch (InterruptedException e) {
            System.out.println(this.iName + " blocked a receiving Ether packet: " + etherPacket);
            e.printStackTrace();
        }
    }

    // 由 Device 放入数据包
    public void putOutputPacket(Ethernet etherPacket) {
        System.out.println(this.iName + " is sending Ether packet: " + etherPacket);
        try {
            System.out.println(this.iName + " blocked a sending Ether packet: " + etherPacket);
            outputQueue.put(etherPacket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**------------------------------------------------------------------------------------------*/

    /**---------------------------------putters:放入（不阻塞）-------------------------------------*/


    // 由 LinkLayer 放入数据包
    public void offerInputPacket(Ethernet etherPacket) {
        if(inputQueue.offer(etherPacket)){
            System.out.println(this.iName + " succeed in receiving Ether packet: " + etherPacket);
        }else{
            System.out.println(this.iName + " failed in receiving Ether packet: " + etherPacket);
        }
    }

    // 由 Device 放入数据包
    public void offerOutputPacket(Ethernet etherPacket) {
        if(outputQueue.offer(etherPacket)){
            System.out.println(this.iName + " succeed in sending Ether packet: " + etherPacket);
        }else{
            System.out.println(this.iName + " failed in sending Ether packet: " + etherPacket);
        }
    }

    /**------------------------------------------------------------------------------------------*/

    /**----------------------------------peekers:查看--------------------------------------------*/

    // 由 Device 查看数据包
    public Ethernet peekInputPacket() {
        return inputQueue.peek();
    }

    // 由 LinkLayer 查看数据包
    public Ethernet peekOutputPacket() {
        return outputQueue.peek();
    }

    /**------------------------------------------------------------------------------------------*/

    /**---------------------------------pollers:取出（不阻塞）-------------------------------------*/

    // 由 Device 取出数据包
    public Ethernet pollInputPacket() {
        return inputQueue.poll();
    }

    // 由 LinkLayer 取出数据包
    public Ethernet pollOutputPacket() {
        return outputQueue.poll();
    }

    /**------------------------------------------------------------------------------------------*/

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
