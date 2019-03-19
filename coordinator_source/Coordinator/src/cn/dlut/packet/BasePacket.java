package cn.dlut.packet;

/**
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public abstract class BasePacket implements IPacket {
    protected IPacket parent;
    protected IPacket payload;

    /**
     * @return the parent
     */
    @Override
    public IPacket getParent() {
        return this.parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    @Override
    public IPacket setParent(final IPacket parent) {
        this.parent = parent;
        return this;
    }

    /**
     * @return the payload
     */
    @Override
    public IPacket getPayload() {
        return this.payload;
    }

    /**
     * @param payload
     *            the payload to set
     */
    @Override
    public IPacket setPayload(final IPacket payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public void resetChecksum() {
        if (this.parent != null) {
            this.parent.resetChecksum();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 6733;
        int result = 1;
        result = prime * result
                + (this.payload == null ? 0 : this.payload.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BasePacket)) {
            return false;
        }
        final BasePacket other = (BasePacket) obj;
        if (this.payload == null) {
            if (other.payload != null) {
                return false;
            }
        } else if (!this.payload.equals(other.payload)) {
            return false;
        }
        return true;
    }

    @Override
    public Object clone() {
        IPacket pkt;
        try {
            pkt = this.getClass().newInstance();
        } catch (final Exception e) {
            throw new RuntimeException("Could not clone packet");
        }
        return pkt;
    }
}
