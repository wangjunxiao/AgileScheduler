package cn.dlut.core.rpc.service.handlers.monitoring;

import java.util.Map;

import cn.dlut.core.rpc.service.handlers.ApiHandler;
import cn.dlut.elements.datapath.PhysicalSwitch;
import cn.dlut.elements.datapath.PhysicalSwitchSerializer;

import cn.dlut.elements.network.PhysicalNetwork;
import cn.dlut.elements.port.PhysicalPort;
import cn.dlut.elements.port.PhysicalPortSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

/**
 * Gets the physical topology in json format.
 */
public class GetPhysicalTopology extends ApiHandler<Object> {

    @SuppressWarnings("unchecked")
    @Override
    public JSONRPC2Response process(final Object params) {
        Map<String, Object> result;
        JSONRPC2Response resp = null;
        // TODO: json objects can be shared with other methods
        final GsonBuilder gsonBuilder = new GsonBuilder();
        // gsonBuilder.setPrettyPrinting();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        gsonBuilder.registerTypeAdapter(PhysicalSwitch.class,
                new PhysicalSwitchSerializer());
        gsonBuilder.registerTypeAdapter(PhysicalPort.class,
                new PhysicalPortSerializer());
        /*
         * gsonBuilder.registerTypeAdapter(PhysicalLink.class, new
         * PhysicalLinkSerializer());
         */

        final Gson gson = gsonBuilder.create();

        result = gson.fromJson(gson.toJson(PhysicalNetwork.getInstance()),
                Map.class);
        resp = new JSONRPC2Response(result, 0);
        return resp;
    }

    @Override
    public JSONRPC2ParamsType getType() {
        return JSONRPC2ParamsType.NO_PARAMS;
    }

}
