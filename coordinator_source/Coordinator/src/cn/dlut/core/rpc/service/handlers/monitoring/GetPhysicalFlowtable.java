package cn.dlut.core.rpc.service.handlers.monitoring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;

import cn.dlut.core.rpc.service.handlers.ApiHandler;
import cn.dlut.core.rpc.service.handlers.HandlerUtils;
import cn.dlut.core.rpc.service.handlers.MonitoringHandler;
import cn.dlut.elements.datapath.PhysicalSwitch;
import cn.dlut.elements.network.PhysicalNetwork;
import cn.dlut.exceptions.InvalidDPIDException;
import cn.dlut.exceptions.MissingRequiredField;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class GetPhysicalFlowtable extends ApiHandler<Map<String, Object>> {

    private JSONRPC2Response resp = null;

    @Override
    public JSONRPC2Response process(final Map<String, Object> params) {
        try {
            final Number dpid = HandlerUtils.<Number>fetchField(
                    MonitoringHandler.DPID, params, false, -1);
            LinkedList<OFFlowStatisticsReply> flows = new LinkedList<OFFlowStatisticsReply>();

            if (dpid.longValue() == -1) {
                HashMap<String, List<Map<String, Object>>> res = new HashMap<String, List<Map<String, Object>>>();
                for (PhysicalSwitch sw : PhysicalNetwork.getInstance()
                        .getSwitches()) {
                    flows = aggregateFlowsBySwitch(sw.getSwitchId());
                    res.put(sw.getSwitchName(), flowModsToMap(flows));
                }
                this.resp = new JSONRPC2Response(res, 0);
            } else {
                flows = aggregateFlowsBySwitch(dpid.longValue());
                this.resp = new JSONRPC2Response(flowModsToMap(flows), 0);
            }

        } catch (ClassCastException | MissingRequiredField e) {
            this.resp = new JSONRPC2Response(new JSONRPC2Error(
                    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
                            + ": Unable to fetch virtual topology : "
                            + e.getMessage()), 0);
        } catch (final InvalidDPIDException e) {
            this.resp = new JSONRPC2Response(new JSONRPC2Error(
                    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
                            + ": Unable to fetch virtual topology : "
                            + e.getMessage()), 0);
        }
        return this.resp;
    }

    @Override
    public JSONRPC2ParamsType getType() {
        return JSONRPC2ParamsType.OBJECT;
    }

    private List<Map<String, Object>> flowModsToMap(
            LinkedList<OFFlowStatisticsReply> flows) {
        final List<Map<String, Object>> res = new LinkedList<Map<String, Object>>();
        for (OFFlowStatisticsReply frep : flows) {
            OFFlowMod fm = new OFFlowMod();
            fm.setActions(frep.getActions());
            fm.setMatch(frep.getMatch());
            res.add(fm.toMap());
        }
        return res;
    }

    private LinkedList<OFFlowStatisticsReply> aggregateFlowsBySwitch(
            long dpid) {
        LinkedList<OFFlowStatisticsReply> flows = new LinkedList<OFFlowStatisticsReply>();
        final PhysicalSwitch sw = PhysicalNetwork.getInstance().getSwitch(dpid);
        if (sw.getFlowStats() != null) {
        	flows.addAll(sw.getFlowStats());
        }
        return flows;
    }

}
