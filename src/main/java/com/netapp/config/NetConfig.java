package com.netapp.config;

public interface NetConfig {
    // NET TOPO:
    // h1   <===>         r1        <===> h2
    // h1_i <---> [ r1_i1 , r1_i2 ] <---> h2_i
    String NET_NAME = "h1_r1_h2";
}
