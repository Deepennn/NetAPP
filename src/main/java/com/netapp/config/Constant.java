package com.netapp.config;

public interface Constant {
    public static final String ARP_CACHE_PREFFIX = "src/main/resources/config/arp_cache/";
    public static final String ARP_CACHE_SUFFIX = ".ac";
    public static final String ROUTE_TABLE_PREFFIX = "src/main/resources/config/route_table/";
    public static final String ROUTE_TABLE_SUFFIX = ".rt";
    public static final String TOPO_FILENAME = "src/main/resources/config/topo/h1_r1_h2.tp";

    // NETWORK TOPO:
    // h1   <===>         r1        <===> h2
    // h1_i <---> [ r1_i1 , r1_i2 ] <---> h2_i

    // r1
    public static final String R1_HOSTNAME = "r1";
    public static final String R1_I1_INAME = "r1_i1";
    public static final String R1_I2_INAME = "r1_i2";
    public static final String R1_I1_IP = "1.0.0.1";
    public static final String R1_I2_IP = "2.0.0.1";
    public static final String R1_I1_MAC = "00:11:22:33:44:55";
    public static final String R1_I2_MAC = "11:22:33:44:55:00";
    public static final String R1_I1_MASK = "255.255.255.0";
    public static final String R1_I2_MASK = "255.255.255.0";

    // h1
    public static final String H1_HOSTNAME = "h1";
    public static final String H1_I_INAME = "h1_i";
    public static final String H1_I_IP = "1.0.0.2";
    public static final String H1_I_MAC = "AA:BB:CC:DD:EE:FF";
    public static final String H1_I_MASK = "255.255.255.0";

    // h2
    public static final String H2_HOSTNAME = "h2";
    public static final String H2_I_INAME = "h2_i";
    public static final String H2_I_IP = "2.0.0.2";
    public static final String H2_I_MAC = "BB:CC:DD:EE:FF:AA";
    public static final String H2_I_MASK = "255.255.255.0";
}
