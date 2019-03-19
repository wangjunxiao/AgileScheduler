package cn.dlut.elements.port;


import cn.dlut.elements.datapath.DPIDandPort;
import cn.dlut.elements.datapath.Switch;
import cn.dlut.elements.link.Link;

import java.util.Arrays;

import cn.dlut.util.MACAddress;

import org.openflow.protocol.OFPhysicalPort;

/**
 * The Class Port.
 *
 * @param <T1>
 *            The Generic Switch type
 * @param <T2>
 *            The Generic Link type
 */

@SuppressWarnings("rawtypes")

public class Port<T1 extends Switch, T2 extends Link> extends OFPhysicalPort {

    protected MACAddress mac;
    protected Boolean isEdge;
    protected T1 parentSwitch;
    protected LinkPair<T2> portLink;

    // TODO: duplexing/speed on port/link???

    /**
     * Instantiates a new port.
     */
    protected Port(final OFPhysicalPort ofPort) {
        super();
        this.portNumber = ofPort.getPortNumber();
        this.hardwareAddress = ofPort.getHardwareAddress();
        this.name = ofPort.getName();
        this.config = ofPort.getConfig();
        this.state = ofPort.getState();
        this.currentFeatures = ofPort.getCurrentFeatures();
        this.advertisedFeatures = ofPort.getAdvertisedFeatures();
        this.supportedFeatures = ofPort.getSupportedFeatures();
        this.peerFeatures = ofPort.getPeerFeatures();
        if (this.hardwareAddress == null) {
            this.hardwareAddress = new byte[] {(byte) 0xDE, (byte) 0xAD,
                    (byte) 0xBE, (byte) 0xEF, (byte) 0xCA, (byte) 0xFE};
        }
        this.mac = new MACAddress(this.hardwareAddress);
        this.isEdge = false;
        this.parentSwitch = null;
        this.portLink = null;
    }

    @Override
    public void setHardwareAddress(final byte[] hardwareAddress) {
        super.setHardwareAddress(hardwareAddress);
        // no way to update MACAddress instances
        this.mac = new MACAddress(hardwareAddress);
    }

    /**
     * Gets the checks if is edge.
     *
     * @return the checks if is edge
     */
    public Boolean isEdge() {
        return this.isEdge;
    }

    /**
     * Sets the checks if is edge.
     *
     * @param isEdge
     *            the new checks if is edge
     */
    public void setEdge(final Boolean isEdge) {
        this.isEdge = isEdge;
    }

    public T1 getParentSwitch() {
        return this.parentSwitch;
    }

    /**
     * Set the link connected to this port.
     *
     * @param link
     */
    public void setInLink(T2 link) {
        if (this.portLink == null) {
            this.portLink = new LinkPair<T2>();
        }
        this.portLink.setInLink(link);
    }

    /**
     * Set the link connected to this port.
     *
     * @param link
     */
    public void setOutLink(T2 link) {
        if (this.portLink == null) {
            this.portLink = new LinkPair<T2>();
        }
        this.portLink.setOutLink(link);
    }

    /**
     * @return The physical link connected to this port
     */
    public LinkPair<T2> getLink() {
        return this.portLink;
    }

    /**
     *
     * @return the highest nominal throughput currently exposed by the port
     */
    public Integer getCurrentThroughput() {
        PortFeatures feature = new PortFeatures(this.currentFeatures);
        return feature.getHighestThroughput();
    }

    @Override
    public String toString() {
        return "PORT:\n- portNumber: " + this.portNumber + "\n- parentSwitch: "
                + this.getParentSwitch().getSwitchName()
                + "\n- hardwareAddress: "
                + MACAddress.valueOf(this.hardwareAddress).toString()
                + "\n- config: " + this.config + "\n- state: " + this.state
                + "\n- currentFeatures: " + this.currentFeatures
                + "\n- advertisedFeatures: " + this.advertisedFeatures
                + "\n- supportedFeatures: " + this.supportedFeatures
                + "\n- peerFeatures: " + this.peerFeatures + "\n- isEdge: "
                + this.isEdge;
    }

    /*
     * should transient features of the Port be taken into account by
     * hashCode()? They prevent ports from being fetched from maps once their
     * status changes.
     */
    @Override
    public int hashCode() {
        final int prime = 307;
        int result = 1;
        result = prime * result + this.advertisedFeatures;
        result = prime * result + this.config;
        result = prime * result + Arrays.hashCode(this.hardwareAddress);
        result = prime * result
                + (this.name == null ? 0 : this.name.hashCode());
        result = prime * result + this.portNumber;
        result = prime * result + this.parentSwitch.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Port)) {
            return false;
        }
        Port other = (Port) obj;
        if (parentSwitch == null) {
            if (other.parentSwitch != null) {
                return false;
            }
        } else if (!parentSwitch.equals(other.parentSwitch)) {
            return false;
        }
        return true;
    }

    public DPIDandPort toDPIDandPort() {
        return new DPIDandPort(this.parentSwitch.getSwitchId(), this.portNumber);
    }
}
