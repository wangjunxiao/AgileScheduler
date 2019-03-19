package cn.dlut.elements.address;


public class PhysicalIPAddress extends IPAddress {

    public PhysicalIPAddress(final Integer ip) {
        this.ip = ip;
    }

    public PhysicalIPAddress(final String ipAddress) {
        super(ipAddress);
    }

}
