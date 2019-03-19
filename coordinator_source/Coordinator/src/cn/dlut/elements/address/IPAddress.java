package cn.dlut.elements.address;

import org.openflow.util.U8;

import cn.dlut.packet.IPv4;

public abstract class IPAddress {
    protected int ip;

    protected IPAddress(final String ipAddress) {
        this.ip = IPv4.toIPv4Address(ipAddress);
    }

    protected IPAddress() {
    }

    public int getIp() {
        return this.ip;
    }

    public void setIp(final int ip) {
        this.ip = ip;
    }

    public String toSimpleString() {
        return U8.f((byte) (this.ip >> 24)) + "." + (this.ip >> 16 & 0xFF)
                + "." + (this.ip >> 8 & 0xFF) + "." + (this.ip & 0xFF);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + (this.ip >> 24) + "."
                + (this.ip >> 16 & 0xFF) + "." + (this.ip >> 8 & 0xFF) + "."
                + (this.ip & 0xFF) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ip;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IPAddress other = (IPAddress) obj;
        if (ip != other.ip) {
            return false;
        }
        return true;
    }
}
