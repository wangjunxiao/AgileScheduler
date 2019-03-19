package cn.dlut.elements.datapath;

public class DPIDandPort {
    long dpid;
    short port;

    public DPIDandPort(final long dpid, final short port) {
        super();
        this.dpid = dpid;
        this.port = port;
    }

    /**
     * @return the dpid
     */
    public long getDpid() {
        return this.dpid;
    }

    /**
     * @param dpid
     *            the dpid to set
     */
    public void setDpid(final long dpid) {
        this.dpid = dpid;
    }

    /**
     * @return the port
     */
    public short getPort() {
        return this.port;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(final short port) {
        this.port = port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (dpid ^ (dpid >>> 32));
        result = prime * result + port;
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
        DPIDandPort other = (DPIDandPort) obj;
        if (dpid != other.dpid) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.dpid + ":" + this.port;
    }
}
