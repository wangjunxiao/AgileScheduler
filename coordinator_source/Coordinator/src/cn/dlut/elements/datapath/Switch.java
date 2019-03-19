package cn.dlut.elements.datapath;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.dlut.core.io.CoordinatorEventHandler;
import cn.dlut.core.io.CoordinatorSendMsg;
import cn.dlut.elements.port.Port;

import org.jboss.netty.channel.Channel;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.statistics.OFDescriptionStatistics;
import org.openflow.util.HexString;

/**
 * The Class Switch.
 *
 * @param <T>
 *            generic type (Port) that is casted in the subclasses
 */

@SuppressWarnings("rawtypes")
public abstract class Switch<T extends Port> implements CoordinatorEventHandler,
        CoordinatorSendMsg {

    // The description of Coordinator stats
    protected OFDescriptionStatistics desc = null;
    // The switch name (converted from the DPID)
    protected String switchName = null;
    // channel status
    protected boolean isConnected = false;
    // The Switch channel descriptor
    protected Channel channel = null;

    /**
     * The port map. Associate all the port instances with the switch. The port
     * number is the key.
     */
    protected HashMap<Short, T> portMap = null;

    /** The features reply message. */
    protected OFFeaturesReply featuresReply = null;

    /** The switch id (DPID). */
    protected Long switchId = (long) 0;

    /**
     * Instantiates a new switch (should be never used).
     *
     * @param switchId
     *            the switchId (long) that represent the DPID
     * @param map
     *            reference to the CoordinatorMap
     */

    protected Switch(final Long switchId) {
        this.switchId = switchId;
        this.switchName = HexString.toHexString(this.switchId);
        this.portMap = new HashMap<Short, T>();
        this.featuresReply = null;
    }

    /**
     * Gets the switch name.
     *
     * @return a user-friendly String that map the switch DPID
     */
    public String getSwitchName() {
        return this.switchName;
    }

    /**
     * Gets the switch info.
     *
     * @return the switch info
     */
    public OFFeaturesReply getFeaturesReply() {
        return this.featuresReply;
    }

    /**
     * Sets the features reply.
     *
     * @param m the new features reply
     */
    public void setFeaturesReply(final OFFeaturesReply m) {
        this.featuresReply = m;
    }

    /**
     * Gets the switch id.
     *
     * @return the switch id
     */
    public Long getSwitchId() {
        return this.switchId;
    }

    /**
     * Returns an unmodifiable copy of the port map.
     */

    public Map<Short, T> getPorts() {
        return Collections.unmodifiableMap(this.portMap);
    }

    /**
     * Gets the port.
     *
     * @param portNumber
     *            the port number
     * @return the port instance
     */
    public T getPort(final Short portNumber) {
        return this.portMap.get(portNumber);
    };

    /**
     * Adds the port. If the port is already present then no action is
     * performed.
     *
     * @param port
     *            the port instance
     * @return true, if successful
     */
    public boolean addPort(final T port) {
        if (this.portMap.containsKey(port.getPortNumber())) {
            return false;
        }
        this.portMap.put(port.getPortNumber(), port);
        return true;
    }

    /**
     * Removes the port.
     *
     * @param portNumber
     *            the port number
     * @return true, if successful
     */
    public boolean removePort(Short portNumber) {
        if (this.portMap.containsKey(portNumber)) {
            this.portMap.remove(portNumber);
            return true;
        }
        return false;
    };

    /**
     * (non-Javadoc)
     *
     */
    public abstract void handleIO(OFMessage msg, Channel channel);  

    /**
     * Starts up the switch.
     *
     * @return true upon success startup.
     */
    public abstract boolean boot();

    /**
     * Removes the switch from the network representation. Removal may be
     * triggered by an API call (in the case of a VirtualSwitch) or disconnection of
     * a switch connected to us (in the case of a PhysicalSwitch).
     */
    public abstract void unregister();

    /**
     * Tear down.
     */
    public abstract void tearDown();
    
    /**
     * Sets the switch channel.
     *
     * @param channel
     *            the new channel
     */
    public abstract void setChannel(final Channel channel);
    
    /**
     * Sets the switch connected.
     *
     * @param isConnected
     *            the new connected
     */
    public abstract void setConnected(final boolean isConnected);
    
    /**
     * Sets the description stats.
     *
     * @param description
     *            the new description stats
     */
    public void setDescriptionStats(final OFDescriptionStatistics description) {
        this.desc = description;

    }

    @Override
    public String getName() {
        return this.switchName + ":" + this.switchId;
    }

    @Override
    public String toString() {
        return "SWITCH:\n- switchId: " + this.switchId + "\n- switchName: "
                + this.switchName + "\n- isConnected: " + this.isConnected;
    }

}
