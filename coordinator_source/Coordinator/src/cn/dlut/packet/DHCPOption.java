package cn.dlut.packet;

import java.util.Arrays;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class DHCPOption {
    protected byte code;
    protected byte length;
    protected byte[] data;

    /**
     * @return the code
     */
    public byte getCode() {
        return this.code;
    }

    /**
     * @param code
     *            the code to set
     */
    public DHCPOption setCode(final byte code) {
        this.code = code;
        return this;
    }

    /**
     * @return the length
     */
    public byte getLength() {
        return this.length;
    }

    /**
     * @param length
     *            the length to set
     */
    public DHCPOption setLength(final byte length) {
        this.length = length;
        return this;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * @param data
     *            the data to set
     */
    public DHCPOption setData(final byte[] data) {
        this.data = data;
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.code;
        result = prime * result + Arrays.hashCode(this.data);
        result = prime * result + this.length;
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DHCPOption)) {
            return false;
        }
        final DHCPOption other = (DHCPOption) obj;
        if (this.code != other.code) {
            return false;
        }
        if (!Arrays.equals(this.data, other.data)) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DHCPOption [code=" + this.code + ", length=" + this.length
                + ", data=" + Arrays.toString(this.data) + "]";
    }
}
