package cn.dlut.packet;

import java.util.Arrays;

/**
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class Data extends BasePacket {
    protected byte[] data;

    /**
     *
     */
    public Data() {
    }

    /**
     * @param data
     */
    public Data(final byte[] data) {
        this.data = data;
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
    public Data setData(final byte[] data) {
        this.data = data;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 1571;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(this.data);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Data)) {
            return false;
        }
        final Data other = (Data) obj;
        if (!Arrays.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }
    
    @Override
    public byte[] serialize() {
        return this.data;
    }

    @Override
    public IPacket deserialize(final byte[] data, final int offset,
            final int length) {
        this.data = Arrays.copyOfRange(data, offset, data.length);
        return this;
    }
}
