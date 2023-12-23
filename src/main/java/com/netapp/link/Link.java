package com.netapp.link;

import com.netapp.device.Iface;

public class Link {

    Iface i1;
    Iface i2;

    public Link(Iface i1, Iface i2) {
        this.i1 = i1;
        this.i2 = i2;
    }

    public Iface getI1() {
        return i1;
    }

    public void setI1(Iface i1) {
        this.i1 = i1;
    }

    public Iface getI2() {
        return i2;
    }

    public void setI2(Iface i2) {
        this.i2 = i2;
    }
}
