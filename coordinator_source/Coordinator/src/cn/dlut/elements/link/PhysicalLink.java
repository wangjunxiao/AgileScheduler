package cn.dlut.elements.link;


import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import cn.dlut.elements.datapath.PhysicalSwitch;
import cn.dlut.elements.port.PhysicalPort;

/**
 * The Class PhysicalLink.
 *
 */
public class PhysicalLink extends Link<PhysicalPort, PhysicalSwitch> implements Comparable<PhysicalLink> {

    private static AtomicInteger linkIds = new AtomicInteger(0);

    @SerializedName("linkId")
    @Expose
    private Integer linkId = null;

    /**
     * Instantiates a new physical link.
     *
     * @param srcPort
     *            the source port
     * @param dstPort
     *            the destination port
     */
    public PhysicalLink(final PhysicalPort srcPort, final PhysicalPort dstPort) {
        super(srcPort, dstPort);
        srcPort.setOutLink(this);
        dstPort.setInLink(this);
        this.linkId = PhysicalLink.linkIds.getAndIncrement();
    }

    public Integer getLinkId() {
        return linkId;
    }

    @Override
    public void unregister() {
        srcPort.setOutLink(null);
        dstPort.setInLink(null);
    }

    public void setLinkId(Integer id) {
        this.linkId = id;
    }

    @Override
    public int compareTo(PhysicalLink o) {
        Long sum1 = this.getSrcSwitch().getSwitchId()
                + this.getSrcPort().getPortNumber();
        Long sum2 = o.getSrcSwitch().getSwitchId()
                + o.getSrcPort().getPortNumber();
        if (sum1 == sum2) {
            return (int) (this.getSrcSwitch().getSwitchId() - o.getSrcSwitch()
                    .getSwitchId());
        } else {
            return (int) (sum1 - sum2);
        }
    }

}
