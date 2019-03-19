package cn.dlut.core.rpc.service.handlers;

import java.util.HashMap;

import cn.dlut.core.rpc.service.handlers.monitoring.GetPhysicalFlowtable;
import cn.dlut.core.rpc.service.handlers.monitoring.GetPhysicalTopology;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

/**
 * Creates handlers for all monitoring API calls, and selects the appropriate
 * handler when processing a request.
 */
public class MonitoringHandler extends AbstractHandler implements
        RequestHandler {

    /**
     * Datapath ID.
     */
    public static final String DPID = "dpid";

    @SuppressWarnings({ "serial", "rawtypes" })
    private HashMap<String, ApiHandler> handlers = new HashMap<String, ApiHandler>() {
        {
            this.put("getPhysicalTopology", new GetPhysicalTopology());
            this.put("getPhysicalFlowtable", new GetPhysicalFlowtable());
        }
    };

    @Override
    public String[] handledRequests() {
        return this.handlers.keySet().toArray(new String[] {});
    }

	@Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public JSONRPC2Response process(final JSONRPC2Request req,
            final MessageContext ctxt) {
        final ApiHandler m = this.handlers.get(req.getMethod());
        if (m != null) {
            if (m.getType() != JSONRPC2ParamsType.NO_PARAMS
                    && m.getType() != req.getParamsType()) {
                return new JSONRPC2Response(new JSONRPC2Error(
                        JSONRPC2Error.INVALID_PARAMS.getCode(), req.getMethod()
                                + " requires: " + m.getType() + "; got: "
                                + req.getParamsType()), req.getID());
            }
            switch (m.getType()) {
            case NO_PARAMS:
                return m.process(null);
            case ARRAY:
                return m.process(req.getPositionalParams());
            case OBJECT:
                return m.process(req.getNamedParams());
            default:
                break;
            }
        }
        return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
    }

}
