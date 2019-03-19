package cn.dlut.packet;

/**
 * Base structures shared by L4 segments (refer to TCP, UDP).
 */
public abstract class AbstractSegment extends BasePacket {

    protected short sourcePort;
    protected short destinationPort;
    protected short checksum;

    /**
     * Gets the source port
     * 
     * @return the sourcePort
     */
    public short getSourcePort() {
        return this.sourcePort;
    }

    /**
     * Sets the source port
     * 
     * @param sourcePort
     *            the sourcePort to set
     */
    public AbstractSegment setSourcePort(final short sourcePort) {
        this.sourcePort = sourcePort;
        return this;
    }

    /**
     * Gets the destination port
     * 
     * @return the destinationPort
     */
    public short getDestinationPort() {
        return this.destinationPort;
    }

    /**
     * Sets the destination port
     * 
     * @param destinationPort
     *            the destinationPort to set
     */
    public AbstractSegment setDestinationPort(final short destinationPort) {
        this.destinationPort = destinationPort;
        return this;
    }

    /**
     * Gets the checksum
     * 
     * @return the checksum
     */
    public short getChecksum() {
        return this.checksum;
    }

    /**
     * Sets the checksum
     * 
     * @param checksum
     *            the checksum to set
     */
    public AbstractSegment setChecksum(final short checksum) {
        this.checksum = checksum;
        return this;
    }

    @Override
    public void resetChecksum() {
        this.checksum = 0;
        super.resetChecksum();
    }

    /**
     * Method for subclass hashCode() to piggyback onto
     */
    public int hashCode(int prime) {
        int result = super.hashCode();
        result = prime * result + this.checksum;
        result = prime * result + this.destinationPort;
        result = prime * result + this.sourcePort;
        return result;
    }
}
