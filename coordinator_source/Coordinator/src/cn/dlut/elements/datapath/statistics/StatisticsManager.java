package cn.dlut.elements.datapath.statistics;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import cn.dlut.core.main.Coordinator;
import cn.dlut.core.io.CoordinatorSendMsg;
import cn.dlut.elements.datapath.PhysicalSwitch;
import cn.dlut.elements.network.PhysicalNetwork;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.Wildcards;
import org.openflow.protocol.statistics.OFFlowStatisticsRequest;
import org.openflow.protocol.statistics.OFPortStatisticsRequest;
import org.openflow.protocol.statistics.OFStatisticsType;

public class StatisticsManager implements TimerTask, CoordinatorSendMsg {

    private HashedWheelTimer timer = null;
    private PhysicalSwitch sw;

    Logger log = LogManager.getLogger(StatisticsManager.class.getName());

    private Integer refreshInterval = 30;
    private boolean stopTimer = false;

    public StatisticsManager(PhysicalSwitch sw) {
        /*
         * Get the timer from the PhysicalNetwork class.
         */
        this.timer = PhysicalNetwork.getTimer();
        this.sw = sw;
        this.refreshInterval = Coordinator.getInstance()
                .getStatsRefresh();
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        log.debug("Collecting stats for {}", this.sw.getSwitchName());
        sendPortStatistics();
        sendFlowStatistics();
        if (!this.stopTimer) {
            log.debug("Scheduling stats collection in {} seconds for {}",
                    this.refreshInterval, this.sw.getSwitchName());
            timeout.getTimer().newTimeout(this, refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    private void sendFlowStatistics() {
        OFStatisticsRequest req = new OFStatisticsRequest();
        req.setStatisticType(OFStatisticsType.FLOW);
        OFFlowStatisticsRequest freq = new OFFlowStatisticsRequest();
        OFMatch match = new OFMatch();
        match.setWildcards(Wildcards.FULL);
        freq.setMatch(match);
        freq.setOutPort(OFPort.OFPP_NONE.getValue());
        freq.setTableId((byte) 0xFF);
        req.setStatistics(Collections.singletonList(freq));
        req.setLengthU(req.getLengthU() + freq.getLength());
        sendMsg(req);
    }

    private void sendPortStatistics() {
        OFStatisticsRequest req = new OFStatisticsRequest();
        req.setStatisticType(OFStatisticsType.PORT);
        OFPortStatisticsRequest preq = new OFPortStatisticsRequest();
        preq.setPortNumber(OFPort.OFPP_NONE.getValue());
        req.setStatistics(Collections.singletonList(preq));
        req.setLengthU(req.getLengthU() + preq.getLength());
        sendMsg(req);
    }

    public void start() {

        /*
         * Initially start polling quickly. Then drop down to configured value
         */
        log.info("Starting Stats collection thread for {}",
                this.sw.getSwitchName());
        timer.newTimeout(this, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        log.info("Stopping Stats collection thread for {}",
                this.sw.getSwitchName());
        this.stopTimer = true;
    }

    @Override
    public void sendMsg(OFMessage msg) {
        sw.sendMsg(msg);
    }

    @Override
    public String getName() {
        return "Statistics Manager (" + sw.getName() + ")";
    }
}
