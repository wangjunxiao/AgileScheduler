package cn.dlut.elements.network;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import cn.dlut.core.main.Coordinator;
import cn.dlut.elements.datapath.PhysicalSwitch;
import cn.dlut.elements.datapath.Switch;
import cn.dlut.elements.link.PhysicalLink;
import cn.dlut.elements.port.PhysicalPort;
import cn.dlut.linkdiscovery.SwitchDiscoveryManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.util.HashedWheelTimer;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPort;

/**
 *
 * Singleton class for physical network. Maintains SwitchDiscoveryManager for
 * each switch in the physical network. Listens for LLDP packets and passes them
 * on to the appropriate SwitchDiscoveryManager. Creates and maintains links
 * after discovery, and switch ports are made discoverable here.
 *
 * TODO: should probably subscribe to PORT UP/DOWN events here.
 *
 */
public final class PhysicalNetwork extends Network<PhysicalSwitch, PhysicalPort, PhysicalLink> {

    private static PhysicalNetwork instance;
    private final ConcurrentHashMap<Long, SwitchDiscoveryManager> discoveryManager;
    private static HashedWheelTimer timer;
    private static Logger log = LogManager.getLogger(PhysicalNetwork.class.getName());
    private static ArrayList<Uplink> uplinks; // TODO

    private PhysicalNetwork() {
        PhysicalNetwork.log.info("Starting network discovery...");
        this.discoveryManager = new ConcurrentHashMap<Long, SwitchDiscoveryManager>();
    }

    public static PhysicalNetwork getInstance() {
        if (PhysicalNetwork.instance == null) {
            PhysicalNetwork.instance = new PhysicalNetwork();
        }
        return PhysicalNetwork.instance;
    }

    public static HashedWheelTimer getTimer() {
        if (PhysicalNetwork.timer == null) {
            PhysicalNetwork.timer = new HashedWheelTimer();
        }
        return PhysicalNetwork.timer;
    }

    public static void reset() {
        log.debug("PhysicalNetwork has been explicitly reset. "
                + "Hope you know what you are doing!!");
        PhysicalNetwork.instance = null;
    }

    /**
     * Add physical switch to topology and make it discoverable.
     *
     * @param sw the switch
     */
    @Override
    public synchronized void addSwitch(final PhysicalSwitch sw) {
        super.addSwitch(sw);
        PhysicalNetwork.log.info("added switch {}", this.getSwitches().toString());
        this.discoveryManager.put(sw.getSwitchId(), new SwitchDiscoveryManager(
                sw, Coordinator.getInstance().getUseBDDP()));
        PhysicalNetwork.log.info("added discovery {}", this.discoveryManager.toString());
    }
    
    /**
     * Handles LLDP packets by passing them on to the appropriate
     * SwitchDisoveryManager (which sent the original LLDP packet).
     *
     * @param msg the LLDP packet in
     * @param the switch
     */
    @SuppressWarnings("rawtypes")
    public void handleLLDP(final OFMessage msg, final Switch sw) {
        // Pass msg to appropriate SwitchDiscoveryManager
        final SwitchDiscoveryManager sdm = this.discoveryManager.get(sw.getSwitchId());
        if (sdm != null) {
            sdm.handleLLDP(msg, sw);
        }
    }

    /**
     * Removes switch from topology discovery and mappings for this network.
     *
     * @param sw the switch
     */
    public boolean removeSwitch(final PhysicalSwitch sw) {
        SwitchDiscoveryManager sdm = this.discoveryManager
                .get(sw.getSwitchId());
        for (PhysicalPort port : sw.getPorts().values()) {
            removePort(sdm, port);
        }
        if (sdm != null) {
            this.discoveryManager.remove(sw.getSwitchId());
        }
        return super.removeSwitch(sw);
    }

    /**
     * Adds port for discovery.
     *
     * @param port the port
     */
    public synchronized void addPort(final PhysicalPort port) {
        SwitchDiscoveryManager sdm = this.discoveryManager.get(port
                .getParentSwitch().getSwitchId());
        if (sdm != null) {
            // Do not run discovery on local OpenFlow port
            if (port.getPortNumber() != OFPort.OFPP_LOCAL.getValue()) {
                sdm.addPort(port);
            }
        }
    }

    /**
     * Removes port from discovery.
     *
     * @param sdm switch discovery manager
     * @param port the port
     */
    public synchronized void removePort(SwitchDiscoveryManager sdm,
            final PhysicalPort port) {
        port.unregister();
        /* remove from topology discovery */
        if (sdm != null) {
            log.info("removing port {}", port.getPortNumber());
            sdm.removePort(port);
        }
        /* remove from this network's mappings */
        PhysicalPort dst = this.neighborPortMap.get(port);
        if (dst != null) {
            this.removeLink(port, dst);
        }
    }

    /**
     * Creates link and adds it to the topology.
     *
     * @param srcPort source port
     * @param dstPort destination port
     */
    public synchronized void createLink(final PhysicalPort srcPort,
            final PhysicalPort dstPort) {
        final PhysicalPort neighbourPort = this.getNeighborPort(srcPort);
        if (neighbourPort == null || !neighbourPort.equals(dstPort)) {
            final PhysicalLink link = new PhysicalLink(srcPort, dstPort);
            super.addLink(link);
            log.info("Adding physical link between {}/{} and {}/{}", link
                    .getSrcSwitch().getSwitchName(), link.getSrcPort()
                    .getPortNumber(), link.getDstSwitch().getSwitchName(), link
                    .getDstPort().getPortNumber());
        } else {
            log.debug("Tried to create invalid link");
        }
    }

    /**
     * Removes link from the topology.
     *
     * @param srcPort source port
     * @param dstPort destination port
     */
    public synchronized void removeLink(final PhysicalPort srcPort,
            final PhysicalPort dstPort) {
        PhysicalPort neighbourPort = this.getNeighborPort(srcPort);
        if ((neighbourPort != null) && (neighbourPort.equals(dstPort))) {
            final PhysicalLink link = super.getLink(srcPort, dstPort);
            super.removeLink(link);
            log.info("Removing physical link between {}/{} and {}/{}", link
                    .getSrcSwitch().getSwitchName(), link.getSrcPort()
                    .getPortNumber(), link.getDstSwitch().getSwitchName(), link
                    .getDstPort().getPortNumber());
            super.removeLink(link);
        } else {
            PhysicalNetwork.log.debug("Tried to remove invalid link");
        }
    }

    /**
     * Acknowledges receipt of discovery probe to sender port.
     *
     * @param port the port
     */
    public void ackProbe(final PhysicalPort port) {
        final SwitchDiscoveryManager sdm = this.discoveryManager.get(port
                .getParentSwitch().getSwitchId());
        if (sdm != null) {
            sdm.ackProbe(port);
        }
    }

    @Override
    public String getName() {
        return "Physical network";
    }

    @Override
    public boolean boot() {
    	log.info("Starting physical network...");
        return true;
    }

    /**
     * Gets the discovery manager for the given switch.
     * TODO use MappingException to deal with null SDMs
     *
     * @param dpid the datapath ID
     * @return the discovery manager instance
     */
    public SwitchDiscoveryManager getDiscoveryManager(long dpid) {
        return this.discoveryManager.get(dpid);
    }

	@Override
	public void sendMsg(OFMessage msg) {
		//TODO
	}

	public static ArrayList<Uplink> getUplinks() {
		return uplinks;
	}

	public static void setUplinks(ArrayList<Uplink> uplinks) {
		PhysicalNetwork.uplinks = uplinks;
	}

}
