package com.netapp.config;

public interface DeviceConfig {
    // file path
    String ARP_CACHE_PREFFIX = "src/main/resources/config/arp_cache/";
    String ARP_CACHE_SUFFIX = ".ac";
    String ROUTE_TABLE_PREFFIX = "src/main/resources/config/route_table/";
    String ROUTE_TABLE_SUFFIX = ".rt";
    String TOPO_PREFFIX = "src/main/resources/config/topo/";
    String TOPO_SUFFIX = ".tp";

    // 路由器 r1
    String R1_HOSTNAME = "r1";
    String R1_I1_INAME = "r1_i1";
    String R1_I2_INAME = "r1_i2";
    String R1_I1_IP = "1.0.0.1";
    String R1_I2_IP = "2.0.0.1";
    String R1_I1_MAC = "00:11:22:33:44:55";
    String R1_I2_MAC = "11:22:33:44:55:00";
    String R1_I1_MASK = "255.255.255.0";
    String R1_I2_MASK = "255.255.255.0";

    // 主机 h1
    String H1_HOSTNAME = "h1";
    String H1_I_INAME = "h1_i";
    String H1_I_IP = "1.0.0.2";
    String H1_I_MAC = "AA:BB:CC:DD:EE:FF";
    String H1_I_MASK = "255.255.255.0";

    // 主机 h2
    String H2_HOSTNAME = "h2";
    String H2_I_INAME = "h2_i";
    String H2_I_IP = "2.0.0.2";
    String H2_I_MAC = "BB:CC:DD:EE:FF:AA";
    String H2_I_MASK = "255.255.255.0";
}
