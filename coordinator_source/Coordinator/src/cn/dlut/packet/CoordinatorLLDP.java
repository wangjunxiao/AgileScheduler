package cn.dlut.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import cn.dlut.elements.datapath.DPIDandPort;
import cn.dlut.elements.datapath.Switch;
import cn.dlut.elements.port.Port;

/**
 * LLDP packets Coordinator uses for discovery of physical network topology.
 * Refer to IEEE Std 802.1ABTM-2009 for more information.
 *
 */
@SuppressWarnings("rawtypes")
public class CoordinatorLLDP extends LLDP {

    // Specified OUI and coordinator name for organizationally specific TLVs
    public static final byte[] OUI = {(byte) 0xa4, 0x23, 0x05};
    public static final String Coordinator_NAME = "Coordinate";
    public static final byte[] LLDP_NICIRA = {0x01, 0x23, 0x20, 0x00, 0x00,
            0x01};
    public static final byte[] LLDP_MULTICAST = {0x01, (byte) 0x80,
            (byte) 0xc2, 0x00, 0x00, 0x0e};
    public static final byte[] BDDP_MULTICAST = {(byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
    public static final short ETHERTYPE_VLAN = (short) 0x8100;

    // TLV constants: type, size and subtype
    // Organizationally specific TLV also have packet offset and contents of TLV
    // header
    private static final byte CHASSIS_TLV_TYPE = 1;
    private static final byte CHASSIS_TLV_SIZE = 7;
    private static final byte CHASSIS_TLV_SUBTYPE = 4;

    private static final byte PORT_TLV_TYPE = 2;
    private static final byte PORT_TLV_SIZE = 3;
    private static final byte PORT_TLV_SUBTYPE = 2;

    private static final byte TTL_TLV_TYPE = 3;
    private static final byte TTL_TLV_SIZE = 2;

    private static final byte NAME_TLV_TYPE = 127;
    // 4 = OUI (3) + subtype (1)
    private static final byte NAME_TLV_SIZE = (byte) (4 + CoordinatorLLDP.Coordinator_NAME.length());
    private static final byte NAME_TLV_SUBTYPE = 1;
    private static final short NAME_TLV_OFFSET = 32;
    private static final short NAME_TLV_HEADER = (short) ((NAME_TLV_TYPE << 9) | NAME_TLV_SIZE);
    // Contents of full name TLV
    private static final byte[] NAME_TLV = ByteBuffer.allocate(NAME_TLV_SIZE + 2)
            .putShort(NAME_TLV_HEADER).put(OUI).put(NAME_TLV_SUBTYPE)
            .put(Coordinator_NAME.getBytes()).array();

    private static final byte DPID_TLV_TYPE = 127;
    private static final byte DPID_TLV_SIZE = (byte) (12); // 12 = OUI (3) + subtype
                                                     // (1) + dpid (8)
    private static final byte DPID_TLV_SUBTYPE = 2;
    private static final short DPID_TLV_HEADER = (short) ((DPID_TLV_TYPE << 9) | DPID_TLV_SIZE);
    // Contents of dpid TLV
    // Note that this does *not* contain the actual dpid since we cannot match
    // on it
    private static final byte[] DPID_TLV = ByteBuffer.allocate(DPID_TLV_SIZE + 2 - 8)
            .putShort(DPID_TLV_HEADER).put(OUI).put(DPID_TLV_SUBTYPE)
            .array();

    // Pre-built contents of both organizationally specific TLVs
    private static final byte[] OUI_TLV = ArrayUtils.addAll(NAME_TLV, DPID_TLV);

    // Default switch, port number and TTL
    private static final byte[] DEFAULT_DPID = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00 };
    private static final short DEFAULT_PORT = 0;
    private static final short DEFAULT_TTL = 120; // in seconds

    // Minimum and Coordinator-generated LLDP packet sizes
    private static final short MINIMUM_LLDP_SIZE = 61;
    // Add 12 for 2-byte header of each TLV and a single EndOfLLDPTLV
    private static final short Coordinator_LLDP_SIZE = (short) (CHASSIS_TLV_SIZE
            + PORT_TLV_SIZE + TTL_TLV_SIZE + NAME_TLV_SIZE + DPID_TLV_SIZE + 12);

    // Field offsets in Coordinator-generated LLDP
    private static final short ETHERTYPE_OFFSET = 12;
    private static final short PORT_OFFSET = 26;
    private static final short DPID_OFFSET = 54;

    // Private member fields
    // Byte arrays for TLV information string
    private ByteBuffer bb;
    private byte[] chassisId = new byte[CHASSIS_TLV_SIZE];
    private byte[] portId = new byte[PORT_TLV_SIZE];
    private byte[] ttl = new byte[TTL_TLV_SIZE];
    private byte[] ouiName = new byte[NAME_TLV_SIZE];
    private byte[] ouiDpid = new byte[DPID_TLV_SIZE];

    // TLVs
    private LLDPTLV chassisTLV;
    private LLDPTLV portTLV;
    private LLDPTLV ttlTLV;
    private LLDPTLV ouiNameTLV;
    private LLDPTLV ouiDpidTLV;
    private List<LLDPTLV> optionalTLVList;

    @SuppressWarnings("unused")
	private Switch sw = null;
    private Port port = null;

    /**
     * Instantiates a new Coordinator LDDP message.
     */
    public CoordinatorLLDP() {
        // Create TLVs
        this.chassisTLV = new LLDPTLV();
        this.portTLV = new LLDPTLV();
        this.ttlTLV = new LLDPTLV();
        this.ouiNameTLV = new LLDPTLV();
        this.ouiDpidTLV = new LLDPTLV();
        this.optionalTLVList = new LinkedList<LLDPTLV>();
        this.optionalTLVList.add(this.ouiNameTLV);
        this.optionalTLVList.add(this.ouiDpidTLV);

        // Add TLVs to LLDP packet
        this.setChassisId(this.chassisTLV);
        this.setPortId(this.portTLV);
        this.setTtl(this.ttlTLV);
        this.setOptionalTLVList(this.optionalTLVList);

        // Set TLVs to default values
        this.setChassisTLV(DEFAULT_DPID);
        this.setPortTLV(DEFAULT_PORT);
        this.setTTLTLV(DEFAULT_TTL);
        this.setOUIName(CoordinatorLLDP.Coordinator_NAME);
        this.setOUIDpid(DEFAULT_DPID);
    }

    /**
     * Sets chassis TLV. Note that we can only put 6 bytes in the chassis ID, so
     * we use another organizationally specific TLV to put the full dpid (see
     * setOUIDpid()).
     *
     * @param dpid the switch DPID
     */
    private void setChassisTLV(final byte[] dpid) {
        this.bb = ByteBuffer.wrap(this.chassisId);
        this.bb.put(CHASSIS_TLV_SUBTYPE);
        for (int i = 2; i < 8; i++) {
            bb.put(dpid[i]);
        }

        this.chassisTLV.setLength(CHASSIS_TLV_SIZE);
        this.chassisTLV.setType(CHASSIS_TLV_TYPE);
        this.chassisTLV.setValue(this.chassisId);
    }

    /**
     * Sets port TLV.
     *
     * @param portNumber the port number
     */
    private void setPortTLV(final short portNumber) {
        this.bb = ByteBuffer.wrap(this.portId);
        this.bb.put(PORT_TLV_SUBTYPE);
        this.bb.putShort(portNumber);

        this.portTLV.setLength(PORT_TLV_SIZE);
        this.portTLV.setType(PORT_TLV_TYPE);
        this.portTLV.setValue(this.portId);
    }

    /**
     * Sets Time To Live TLV.
     *
     * @param time the time to live
     */
    private void setTTLTLV(final short time) {
        this.bb = ByteBuffer.wrap(this.ttl);
        this.bb.putShort(time);

        this.ttlTLV.setLength(TTL_TLV_SIZE);
        this.ttlTLV.setType(TTL_TLV_TYPE);
        this.ttlTLV.setValue(this.ttl);
    }

    /**
     * Set. organizationally specific TLV for Coordinator name (subtype 1).
     *
     * @param name the name
     */
    private void setOUIName(final String name) {
        this.bb = ByteBuffer.wrap(ouiName);
        this.bb.put(CoordinatorLLDP.OUI);
        this.bb.put(NAME_TLV_SUBTYPE);
        this.bb.put(name.getBytes());

        this.ouiNameTLV.setLength(NAME_TLV_SIZE);
        this.ouiNameTLV.setType(NAME_TLV_TYPE);
        this.ouiNameTLV.setValue(ouiName);
    }

    /**
     * Sets organizationally specific TLV for Coordinator full dpid (subtype 2).
     *
     * @param dpid the switch DPID
     */
    private void setOUIDpid(final byte[] dpid) {
        this.bb = ByteBuffer.wrap(ouiDpid);
        this.bb.put(CoordinatorLLDP.OUI);
        this.bb.put(DPID_TLV_SUBTYPE);
        this.bb.put(dpid);

        this.ouiDpidTLV.setLength(DPID_TLV_SIZE);
        this.ouiDpidTLV.setType(DPID_TLV_TYPE);
        this.ouiDpidTLV.setValue(ouiDpid);
    }

    /**
     * Sets switch DPID in LLDP packet.
     *
     * @param sw the switch instance
     */
    public void setSwitch(Switch sw) {
        this.sw = sw;
        final byte[] dpid = ByteBuffer.allocate(8).putLong(sw.getSwitchId())
                .array();
        this.setChassisTLV(dpid);
        this.setOUIDpid(dpid);
    }

    /**
     * Sets port in LLDP packet.
     *
     * @param port the port instance
     */
    public void setPort(Port port) {
        this.port = port;
        short portNumber = this.port.getPortNumber();
        this.setPortTLV(portNumber);
    }

    /**
     * Checks if LLDP packet has correct size, LLDP multicast address, and
     * ethertype. Packet assumed to have Ethernet header.
     *
     * @param packet
     * @return true if packet is LLDP, false otherwise
     */
    public static boolean isLLDP(final byte[] packet) {
        // Does packet exist and does it have the mininum size?
        if (packet == null || packet.length < MINIMUM_LLDP_SIZE) {
            return false;
        }

        // Packet has LLDP multicast destination address?
        final ByteBuffer bb = ByteBuffer.wrap(packet);
        final byte[] dst = new byte[6];
        bb.get(dst);

        if (!(Arrays.equals(dst, CoordinatorLLDP.LLDP_NICIRA)
                || Arrays.equals(dst, CoordinatorLLDP.LLDP_MULTICAST) || Arrays.equals(
                dst, CoordinatorLLDP.BDDP_MULTICAST))) {

            return false;
        }

        // Fetch ethertype, skip VLAN tag if it's there
        short etherType = bb.getShort(ETHERTYPE_OFFSET);
        if (etherType == ETHERTYPE_VLAN) {
            etherType = bb.getShort(ETHERTYPE_OFFSET + 4);
        }

        // Check ethertype
        if (etherType == Ethernet.TYPE_LLDP) {
            return true;
        }
        if (etherType == Ethernet.TYPE_BSN) {
            return true;
        }

        return false;

    }

    /**
     * Checks if packet has size of Coordinator-generated LLDP, and correctness of two
     * organizationally specific TLVs that use specified OUI. Assumes packet is
     * valid LLDP packet
     *
     * @param packet
     * @return
     */
    public static boolean isCoordinatorLLDP(byte[] packet) {
        if (packet.length < Coordinator_LLDP_SIZE) {
            return false;
        }

        // Extra offset due to VLAN tag
        final ByteBuffer bb = ByteBuffer.wrap(packet);
        int offset = 0;
        if (bb.getShort(ETHERTYPE_OFFSET) != Ethernet.TYPE_LLDP
                && bb.getShort(ETHERTYPE_OFFSET) != Ethernet.TYPE_BSN) {
            offset = 4;
        }

        // Compare packet's organizationally specific TLVs to the expected
        // values
        for (int i = 0; i < OUI_TLV.length; i++) {
            if (packet[NAME_TLV_OFFSET + offset + i] != OUI_TLV[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Extracts dpid and port from Coordinator-generated LLDP packet.
     *
     * @param packet
     * @return Dpid and port
     */
    public static DPIDandPort parseLLDP(final byte[] packet) {
        final ByteBuffer bb = ByteBuffer.wrap(packet);

        // Extra offset due to VLAN tag
        int offset = 0;
        if (bb.getShort(ETHERTYPE_OFFSET) != Ethernet.TYPE_LLDP
                && bb.getShort(ETHERTYPE_OFFSET) != Ethernet.TYPE_BSN) {
            offset = 4;
        }

        final short port = bb.getShort(PORT_OFFSET + offset);
        final long dpid = bb.getLong(DPID_OFFSET + offset);

        return new DPIDandPort(dpid, port);
    }
}
