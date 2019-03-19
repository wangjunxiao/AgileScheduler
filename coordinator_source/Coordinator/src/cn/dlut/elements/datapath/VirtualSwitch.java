package cn.dlut.elements.datapath;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import cn.dlut.core.main.CoordinatorQueue;
import cn.dlut.core.main.Main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFGetConfigReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFSetConfig;
import org.openflow.protocol.OFStatisticsReply;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.OFSwitchConfig;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.statistics.OFDescriptionStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;

import cn.dlut.elements.controller.Controller;
import cn.dlut.elements.network.PhysicalNetwork;
import cn.dlut.elements.port.PhysicalPort;

/**
 * The base virtual switch.
 */
public  class VirtualSwitch extends Switch<PhysicalPort> {

    private static Logger log = LogManager.getLogger(VirtualSwitch.class.getName());
    
    /**
     * Datapath description string.
     * TODO: should this be made specific per type of virtual switch?
     */
    public static final String DPDESCSTRING = "Coordinator vSwitch";
    protected static int supportedActions = 0xFFF;
    protected static int bufferDimension = 4096;
    // default in spec is 128
    protected Short missSendLen = 128;
    // The backoff counter for this switch when unconnected
    private AtomicInteger backOffCounter = null;
    //protected LRULinkedHashMap<Integer, OFPacketIn> bufferMap;
    
    private PhysicalSwitch physicalSwitch = null;
    private Controller ctrl = null;
    
    
    /**
     * Instantiates a new virtual switch.
     *
     * @param switchId the switch id
     * @param tenantId the tenant id
     */
    public VirtualSwitch(Long switchId) {
        super(switchId);
        this.missSendLen = 0;
        this.backOffCounter = new AtomicInteger();
        this.resetBackOff();
    }
   
    public void setPhysicalSwitch(PhysicalSwitch phy) {
    	this.physicalSwitch=phy;
    }
    
    public PhysicalSwitch getPhysicalSwitch() {
    	return this.physicalSwitch;
    }

    public void setController(Controller c){
    	this.ctrl=c;
    }
    
    public Controller getController(){
    	return this.ctrl;
    }
    
    /**
     * Gets the miss send len.
     *
     * @return the miss send len
     */
    public short getMissSendLen() {
        return this.missSendLen;
    }

    /**
     * Sets the miss send len.
     *
     * @param missSendLen
     *            the miss send len
     * @return true, if successful
     */
    public boolean setMissSendLen(final Short missSendLen) {
        this.missSendLen = missSendLen;
        return true;
    }

    /**
     * Resets the backoff counter.
     */
    public void resetBackOff() {
        this.backOffCounter.set(-1);
    }

    /**
     * Increments the backoff counter.
     *
     * @return the backoff counter
     */
    public int incrementBackOff() {
        return this.backOffCounter.incrementAndGet();
    }
        
    /**
     * Generate features reply.
     */
    public void generateFeaturesReply() {
    	OFFeaturesReply ofReply=new OFFeaturesReply();
    	ofReply=this.physicalSwitch.getFeaturesReply();
        this.setFeaturesReply(ofReply);
    }
    
    /**
     * Boots virtual switch by connecting it to the controller.
     *
     * @return true if successful, false otherwise
     */
    @Override
    public boolean boot() {
        this.generateFeaturesReply();
        return true;
    }
    
	/**
     * Unregisters switch from the mapping,
     * and removes all virtual elements that rely on this switch.
     */
    public void unregister() {
    	this.setConnected(false);
    	this.ctrl.getVirtualSwitch().remove(this);
    	this.physicalSwitch.getVirtualSwitchSet().remove(this);
    	this.tearDown();
    }

    @Override
    public void tearDown() {
    	VirtualSwitch.log.info("VirtualSwitch {} is destroyed",
                this.featuresReply.getDatapathId());
        this.channel.disconnect();
    }
    
    @Override
    public String toString() {
        return "SWITCH: switchId: " + this.switchId + " - switchName: "
                + this.switchName + " - isConnected: " + this.isConnected
                 + " - missSendLength: "+ this.missSendLen +" - capabilities: "
                + this.getPhysicalSwitch().getFeaturesReply().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((switchId == null) ? 0 : switchId.hashCode());
        return result;
    }

    @Override
    public void sendMsg(final OFMessage msg) {
        if (this.isConnected ) {
            this.channel.write(Collections.singletonList(msg));
        } else {
            // TODO: we probably should install a drop rule here.
            log.warn("Virtual switch {} is not active or is not connected to a controller", switchName);
        }
    }
    
    public boolean isConnected(){
    	return this.isConnected;
    }
   
	@Override
	public void setConnected(boolean isConnected) {
		this.isConnected=isConnected;
	}
		
	/**
     * Sets the channel.
     *
     * @param channel the channel
     */
    public void setChannel(Channel channel) {
    	this.channel=channel;
    }

    //get current channel of Virtual Switch, use for Coordinator.deleteController
    public Channel getChannel(){
    	return this.channel;
    }
	
	@Override
	public OFFeaturesReply getFeaturesReply() {
		return this.getPhysicalSwitch().getFeaturesReply();
	}
	
    public void handlePacketOut(OFMessage msg,Channel channel){
    	OFPacketOut pktOut=(OFPacketOut)msg;
    	CoordinatorQueue.popCtrlPktIn(this.getController(), pktOut.getBufferId());
    	this.getPhysicalSwitch().sendMsg(pktOut);
    }
    
	public void handleFlowMod(OFMessage msg,Channel channel){
		this.physicalSwitch.sendMsg(msg);
	}
    
	//handle LLDP request from northbound controller
	public void handleLLDP(OFMessage msg) {
		final OFPacketOut po = (OFPacketOut) msg;
        final byte[] pkt = po.getPacketData();
        // Create LLDP response for each output action port
        for (final OFAction action : po.getActions()) {
            try {
                final short portNumber = ((OFActionOutput) action).getPort();
                final PhysicalPort srcPort = (PhysicalPort) this.getPhysicalSwitch().getPort(portNumber);
                final PhysicalPort dstPort = PhysicalNetwork.getInstance().getNeighborPort(srcPort);
                if (dstPort != null) {
                    final OFPacketIn pi = new OFPacketIn();
                    pi.setBufferId(OFPacketOut.BUFFER_ID_NONE);
                    // Get input port from pkt_out
                    pi.setInPort(dstPort.getPortNumber());
                    pi.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
                    pi.setPacketData(pkt);
                    pi.setTotalLength((short) (OFPacketIn.MINIMUM_LENGTH + pkt.length));
                    for(VirtualSwitch vsw:(dstPort.getParentSwitch().getVirtualSwitchSet())) {
                    	vsw.sendMsg(pi);
                    }
                }
            } catch (final ClassCastException c) {
                // ignore non-ActionOutput pkt_out's
                continue;
            }
        }
	}

    @Override
    public void handleIO(final OFMessage msg, Channel channel) {
       switch(msg.getType()){	
		
		case SET_CONFIG:
			this.setMissSendLen(((OFSetConfig) msg).getMissSendLength());
			VirtualSwitch.log.info("Setting miss send length to {} for VirtualSwitch {}",
                    ((OFSwitchConfig) msg).getMissSendLength(), this.getSwitchName());
            break;
		case PACKET_OUT:
			this.handlePacketOut(msg,channel);
			break;
            
        case STATS_REQUEST:
        	switch (((OFStatisticsRequest)msg).getStatisticType()) {
			case DESC:
				final OFStatisticsReply reply = new OFStatisticsReply();
				final OFDescriptionStatistics desc = new OFDescriptionStatistics();
				desc.setDatapathDescription(VirtualSwitch.DPDESCSTRING);
		        desc.setHardwareDescription("virtual hardware");
		        desc.setManufacturerDescription("cn.dlut");
		        desc.setSerialNumber(this.getSwitchName());
		        desc.setSoftwareDescription(Main.VERSION);
				
		        reply.setXid(msg.getXid());
		        reply.setLengthU(reply.getLength() + desc.getLength());
		        reply.setStatisticType(OFStatisticsType.DESC);
		        reply.setStatistics(Collections.singletonList(desc));
		        this.sendMsg(reply);
	            break;
	        
			case FLOW:
				/*OFStatisticsReply ofsreply_flow = new OFStatisticsReply();
				Integer flowstats_length = 0; 
			
				for (OFFlowStatisticsReply s : this.physicalSwitch.getFlowStats()) {
					flowstats_length += s.getLength();
				}
				ofsreply_flow.setXid(msg.getXid());
				ofsreply_flow.setStatisticType(OFStatisticsType.FLOW);
				ofsreply_flow.setStatistics(this.physicalSwitch.getFlowStats());
				ofsreply_flow.setLengthU(OFStatisticsReply.MINIMUM_LENGTH + flowstats_length);
				this.sendMsg(ofsreply_flow);*/
			    break;
			    
			case PORT:
				break;
				
			case AGGREGATE:
				/*final OFStatistics stat = ((OFStatisticsRequest)msg).getStatistics().get(0);
				OFAggregateStatisticsReply agstat = new OFAggregateStatisticsReply();
			    HashSet<Long> uniqueCookies = new HashSet<Long>();
			    // the -1 is for beacon...
			    if((((OFAggregateStatisticsRequest) stat).getMatch().getWildcardObj().isFull() 
			    		|| ((OFAggregateStatisticsRequest) stat).getMatch().getWildcards() == -1)
		                && ((OFAggregateStatisticsRequest) stat).getOutPort() == OFPort.OFPP_NONE.getValue()) {
			    	agstat.setByteCount(0);
			    	agstat.setPacketCount(0);
			    	agstat.setFlowCount(0);
			    	if (this.physicalSwitch.getFlowStats() != null) {
			    		agstat.setFlowCount(this.physicalSwitch.getFlowStats().size());
	                    for (OFFlowStatisticsReply s : this.physicalSwitch.getFlowStats()) {
	                        if (!uniqueCookies.contains(s.getCookie())) {
	                            agstat.setByteCount(agstat.getByteCount() + s.getByteCount());
	                            agstat.setPacketCount(agstat.getPacketCount() + s.getPacketCount());
	                            uniqueCookies.add(s.getCookie());
	                        }
	                    }
	                }
			    }
			    OFStatisticsReply ofsreply_ag = new OFStatisticsReply();
		    	ofsreply_ag.setXid(msg.getXid());
		    	ofsreply_ag.setStatisticType(OFStatisticsType.AGGREGATE);
		    	ofsreply_ag.setStatistics(Collections.singletonList(agstat));
		    	ofsreply_ag.setLengthU(OFStatisticsReply.MINIMUM_LENGTH + agstat.getLength());
		        this.sendMsg(ofsreply_ag);
			    break;*/
				
			case TABLE:
				break;
			case VENDOR:
				break;
			case QUEUE:
				break;
				
	        default:
	        	VirtualSwitch.log.info("HandleClientIO handle OFStatistics {} for default and break", 
	        			((OFStatisticsRequest)msg).getStatisticType());
	        	break;
	        }
        	break;
        	
        case FLOW_MOD:
        	//simulate for heavy ofmessage handler load, since no flow mod can install in southbound switch.
        	//this.handleFlowMod(msg,channel);
        	break;
            
        case GET_CONFIG_REQUEST:
			final OFGetConfigReply configreply = new OFGetConfigReply();
			configreply.setMissSendLength(this.getMissSendLen());
	        configreply.setXid(msg.getXid());
	        channel.write(Collections.singletonList(configreply));
			break; 
		
        case VENDOR:
        	break;
			
		default:
			VirtualSwitch.log.info("HandleClientIO handle OFMessage {} for default and break", msg.getType());
			break;
		}
    }
}
