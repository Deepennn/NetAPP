package com.netapp.config;

import com.netapp.device.Device;
import com.netapp.device.Iface;
import com.netapp.device.NetIface;
import com.netapp.device.host.Host;
import com.netapp.device.router.Router;

import java.util.HashMap;
import java.util.Map;

import static com.netapp.config.DeviceConfig.*;

public class DeviceFactory {
    public static Map<String, Device> provide() {
        // 创建路由器 r1
        NetIface r1_i1 = new NetIface(R1_I1_INAME,R1_I1_IP,R1_I1_MAC,R1_I1_MASK);
        NetIface r1_i2 = new NetIface(R1_I2_INAME,R1_I2_IP,R1_I2_MAC,R1_I2_MASK);
        Map<String, Iface> interfaces_r1 = new HashMap<>();
        interfaces_r1.put(R1_I1_INAME,r1_i1);
        interfaces_r1.put(R1_I2_INAME,r1_i2);
        Router r1 = new Router(R1_HOSTNAME, interfaces_r1);

        // 创建主机 h1
        NetIface h1_i = new NetIface(H1_I_INAME,H1_I_IP,H1_I_MAC,H1_I_MASK);
        Map<String,Iface> interfaces_h1 = new HashMap<>();
        interfaces_h1.put(H1_I_INAME,h1_i);
        Host h1 = new Host(H1_HOSTNAME, interfaces_h1, R1_I1_IP);

        // 创建主机 h2
        NetIface h2_i = new NetIface(H2_I_INAME,H2_I_IP,H2_I_MAC,H2_I_MASK);
        Map<String,Iface> interfaces_h2 = new HashMap<>();
        interfaces_h2.put(H2_I_INAME,h2_i);
        Host h2 = new Host(H2_HOSTNAME, interfaces_h2, R1_I2_IP);

        Map<String, Device> devices = new HashMap<>();
        devices.put(r1.getHostname(),r1);
        devices.put(h1.getHostname(),h1);
        devices.put(h2.getHostname(),h2);
        return devices;
    }
}
