package com.netapp.config;

import com.netapp.net.Net;

import static com.netapp.config.NetConfig.NET_NAME;

public class NetFactory {
    public static Net provide(){
        return new Net(NET_NAME, DeviceFactory.provide());
    }
}
