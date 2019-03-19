package cn.dlut.elements.datapath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.openflow.protocol.OFError;
import org.openflow.protocol.OFError.OFBadActionCode;
import org.openflow.protocol.OFError.OFBadRequestCode;
import org.openflow.protocol.OFError.OFErrorType;
import org.openflow.protocol.OFError.OFFlowModFailedCode;
import org.openflow.protocol.OFError.OFHelloFailedCode;
import org.openflow.protocol.OFError.OFPortModFailedCode;
import org.openflow.protocol.OFError.OFQueueOpFailedCode;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFStatisticsReply;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;
import org.openflow.protocol.statistics.OFPortStatisticsReply;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.util.LRULinkedHashMap;

import cn.dlut.core.main.Coordinator;
import cn.dlut.core.main.CoordinatorQueue;
import cn.dlut.elements.controller.Controller;
import cn.dlut.elements.datapath.statistics.StatisticsManager;
import cn.dlut.elements.network.PhysicalNetwork;
import cn.dlut.elements.port.PhysicalPort;

/**
 * The Class PhysicalSwitch.
 */
public class PhysicalSwitch extends Switch<PhysicalPort> {

    private static Logger log = LogManager.getLogger(PhysicalSwitch.class.getName());
    private StatisticsManager statsMan = null;
    private AtomicReference<Map<Short, OFPortStatisticsReply>> portStats = null;
    private AtomicReference<List<OFFlowStatisticsReply>> flowStats = null;
    private List<VirtualSwitch> virtualswitches = null;
    
    /**
     * Datapath description string.
     * TODO: should this be made specific per type of virtual switch?
     */
    
    protected LRULinkedHashMap<Integer, OFPacketIn> bufferMap;
    protected static int bufferDimension = 4096;
    private AtomicInteger bufferId = null;
    

    /**
     * Instantiates a new physical switch.
     *
     * @param switchId
     *            the switch id
     */
    public PhysicalSwitch(final long switchId) {
        super(switchId);
        this.portStats = new AtomicReference<Map<Short, OFPortStatisticsReply>>();
        this.flowStats = new AtomicReference<List<OFFlowStatisticsReply>>();
        this.statsMan = new StatisticsManager(this);
        this.bufferMap = new LRULinkedHashMap<Integer, OFPacketIn>(PhysicalSwitch.bufferDimension);
        this.bufferId = new AtomicInteger(1);
        this.virtualswitches = new ArrayList<VirtualSwitch>();
    }

    // add mapping between PhysicalSwitch and VirtualSwitch
    public void addVirtualSwitchSet(VirtualSwitch virsw) {
    	this.virtualswitches.add(virsw);
    }
    public void deleteVirtualSwitchSet(VirtualSwitch virsw) {
    	this.virtualswitches.remove(virsw);
    }
    public List<VirtualSwitch> getVirtualSwitchSet() {
    	return this.virtualswitches;
    }

    /**
     * Fill port map. Assume all ports are edges until discovery says otherwise.
     */
    protected void fillPortMap() {
        for (final OFPhysicalPort port : this.featuresReply.getPorts()) {
            final PhysicalPort physicalPort = new PhysicalPort(port, this, true);
            this.addPort(physicalPort);
        }
    }

    @Override
    public boolean addPort(final PhysicalPort port) {
        final boolean result = super.addPort(port);
        if (result) {
            PhysicalNetwork.getInstance().addPort(port);
        }
        return result;
    }

    /**
     * Removes the specified port from this PhysicalSwitch. This includes
     * removal from the switch's port map, topology discovery, and the
     * PhysicalNetwork topology.
     *
     * @param port the physical port instance
     * @return true if successful, false otherwise
     */
    public boolean removePort(final PhysicalPort port) {
        final boolean result = super.removePort(port.getPortNumber());
        if (result) {
            PhysicalNetwork pnet = PhysicalNetwork.getInstance();
            pnet.removePort(pnet.getDiscoveryManager(this.getSwitchId()), port);
        }
        return result;
    }

    public boolean boot() {
        PhysicalSwitch.log.info("Switch connected with dpid {}, name {} and type {}",
                this.featuresReply.getDatapathId(), this.getSwitchName(),
                this.desc.getHardwareDescription());
        PhysicalNetwork.getInstance().addSwitch(this);
        this.fillPortMap();
        this.statsMan.start();
        return true;
    }

    /**
     * Removes this PhysicalSwitch from the network. Also removes associated
     * ports, links, and virtual elements mapped to it (VirtualSwitch, etc.).
     */
    @Override
    public void unregister() {
    	//destroy related virtual switches
    	while (!this.getVirtualSwitchSet().isEmpty()) {
    		this.getVirtualSwitchSet().get(0).unregister();
    	}
        /* try to remove this switch from network */
        PhysicalNetwork.getInstance().removeSwitch(this);
        this.portMap.clear();
        this.tearDown();
    }
    
    @Override
    public void tearDown() {
        PhysicalSwitch.log.info("Switch disconnected {} ",
                this.featuresReply.getDatapathId());
        this.statsMan.stop();
        this.channel.disconnect();
    }

    @Override
    public void sendMsg(final OFMessage msg) {
        if ((this.channel.isOpen()) && (this.isConnected)) {
            this.channel.write(Collections.singletonList(msg));
        }
    }

    @Override
    public String toString() {
        return "DPID : "
                + this.switchId
                + ", remoteAddr : "
                + ((this.channel == null) ? "None" : this.channel
                        .getRemoteAddress().toString());
    }

    /**
     * Gets the port.
     *
     * @param portNumber
     *            the port number
     * @return the port instance
     */
    @Override
    public PhysicalPort getPort(final Short portNumber) {
        return this.portMap.get(portNumber);
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof PhysicalSwitch) {
            return this.switchId == ((PhysicalSwitch) other).switchId;
        }
        return false;
    }

    public void setPortStatistics(Map<Short, OFPortStatisticsReply> stats) {
        this.portStats.set(stats);
    }

    public void setFlowStatistics(List<OFFlowStatisticsReply> stats) {
        this.flowStats.set(stats);
    }

    public List<OFFlowStatisticsReply> getFlowStats() {
        List<OFFlowStatisticsReply> stats = this.flowStats.get();
        if (stats != null) {
            return Collections.unmodifiableList(stats);
        }
        return null;
    }

    public OFPortStatisticsReply getPortStat(short portNumber) {
        Map<Short, OFPortStatisticsReply> stats = this.portStats.get();
        if (stats != null) {
            return stats.get(portNumber);
        }
        return null;
    }

    public void removeFlowMods(OFStatisticsReply msg) {
        short port = (short) (msg.getXid() & 0xFFFF);
        for (OFStatistics stat : msg.getStatistics()) {
            OFFlowStatisticsReply reply = (OFFlowStatisticsReply) stat;
            if (port != 0) {
                sendDeleteFlowMod(reply, port);
                if (reply.getMatch().getInputPort() == port) {
                    sendDeleteFlowMod(reply, OFPort.OFPP_NONE.getValue());
                }
            } else {
                sendDeleteFlowMod(reply, OFPort.OFPP_NONE.getValue());
            }
        }
    }

    private void sendDeleteFlowMod(OFFlowStatisticsReply reply, short port) {
        OFFlowMod dFm = new OFFlowMod();
        dFm.setCommand(OFFlowMod.OFPFC_DELETE_STRICT);
        dFm.setMatch(reply.getMatch());
        dFm.setOutPort(port);
        dFm.setLengthU(OFFlowMod.MINIMUM_LENGTH);
        this.sendMsg(dFm);
    }
    
    /**
     * Adds a packet_in to the buffer map and returns a unique buffer ID.
     *
     * @param pktIn the packet_in
     * @return the buffer ID
     */
    public synchronized int addToBufferMap(final OFPacketIn pktIn) {
        // TODO: this isn't thread safe... fix it
        this.bufferId.compareAndSet(PhysicalSwitch.bufferDimension, 0);
        this.bufferMap.put(this.bufferId.get(), pktIn);
        return this.bufferId.getAndIncrement();
    }

    /**
     * Gets a packet_in from a given buffer ID.
     *
     * @param bufId the buffer ID
     * @return packet_in packet
     */
    public OFPacketIn getFromBufferMap(final Integer bufId) {
        return this.bufferMap.get(bufId);
    }

	@Override
	public void handleIO(OFMessage msg, Channel channel) {
		switch(msg.getType()) {
			case FLOW_REMOVED:
				break;
			case GET_CONFIG_REPLY://do nothing 
				break;
			case PACKET_IN:
				// TODO: Configure more Packet In message scheduling arithmetic of Coordinator 
				switch(Coordinator.getCoordinatorArithmetic()) {
		  			case 1: 		
		  				//Shortest Queue Length First
		  				this.handlePacketInBySQLF(msg);
		  				break;
		  			default:
		  				PhysicalSwitch.log.error("without this kind of coordinator arithmetic-{}!", 
		  						Coordinator.coordinator_arithmetic);
				}
				break;
			case PORT_STATUS:
				break;
			case QUEUE_GET_CONFIG_REPLY:
				break;
			case STATS_REPLY:
				switch(((OFStatisticsReply)msg).getStatisticType()) {  
					// TODO
					case TABLE:
					case VENDOR:
					case FLOW:
					case PORT:
					case QUEUE:
					case DESC:	
					case AGGREGATE:
						break;
					default:
						PhysicalSwitch.log.info("HandleSwitchIO handle OFStatistics {} for default and break",
								((OFStatisticsRequest)msg).getStatisticType());
				}
				break;
			case ERROR:
				PhysicalSwitch.log.info(this.getErrorString((OFError)msg));
				break;
			case VENDOR:
				/*log.warn("Received Role message {} from switch {}", msg, this.switchName);*/
				break;
			default:
				PhysicalSwitch.log.info("HandleSwitchIO handle OFStatistics {} for default and break", 
						((OFStatisticsRequest)msg).getStatisticType());
		}
	}

	// TODO : more scheduling arithmetic
	public void handlePacketInBySQLF(OFMessage msg){
    	if (CoordinatorQueue.getCtrlPktIn().keySet().isEmpty())
    		return ;
		OFPacketIn pktIn = (OFPacketIn)msg;
		/* ******** Shortest Queue Length First based coordinate arithmetic ******** start */
		Controller minPktIn_controller = CoordinatorQueue.getMinQueueController(this.switchId);
		
		log.info("select controller:"+minPktIn_controller.toSimpleString()+" with Pktin:"+
				CoordinatorQueue.getCtrlPktIn().get(minPktIn_controller).size());
		
		CoordinatorQueue.pushCtrlPktIn(minPktIn_controller, pktIn.getBufferId());
		for (VirtualSwitch virsw : this.getVirtualSwitchSet()) {
			if(virsw.getController() == minPktIn_controller) {
				virsw.sendMsg(pktIn);
				break;
			}
		}
		/* ******** Shortest Queue Length First based coordinate arithmetic ******** end */
	}
	
	private String getErrorString(OFError error) {
        // TODO: this really should be OFError.toString. Sigh.
        int etint = 0xffff & error.getErrorType();
        if (etint < 0 || etint >= OFErrorType.values().length) {
            return String.format("Unknown error type %d", etint);
        }
        OFErrorType et = OFErrorType.values()[etint];
        switch (et) {
        case OFPET_HELLO_FAILED:
            OFHelloFailedCode hfc = OFHelloFailedCode.values()[0xffff & error
                    .getErrorCode()];
            return String.format("Error %s %s", et, hfc);
        case OFPET_BAD_REQUEST:
            OFBadRequestCode brc = OFBadRequestCode.values()[0xffff & error
                    .getErrorCode()];
            return String.format("Error %s %s", et, brc);
        case OFPET_BAD_ACTION:
            OFBadActionCode bac = OFBadActionCode.values()[0xffff & error
                    .getErrorCode()];
            return String.format("Error %s %s", et, bac);
        case OFPET_FLOW_MOD_FAILED:
            OFFlowModFailedCode fmfc = OFFlowModFailedCode.values()[0xffff & error
                    .getErrorCode()];
            return String.format("Error %s %s", et, fmfc);
        case OFPET_PORT_MOD_FAILED:
            OFPortModFailedCode pmfc = OFPortModFailedCode.values()[0xffff & error
                    .getErrorCode()];
            return String.format("Error %s %s", et, pmfc);
        case OFPET_QUEUE_OP_FAILED:
            OFQueueOpFailedCode qofc = OFQueueOpFailedCode.values()[0xffff & error
                    .getErrorCode()];
            return String.format("Error %s %s", et, qofc);
        case OFPET_VENDOR_ERROR:
            // no codes known for vendor error
            return String.format("Error %s", et);
        default:
            break;
        }
        return null;
    }

	public void setChannel(Channel channel) {
		this.channel=channel;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected=isConnected;
	}

}
