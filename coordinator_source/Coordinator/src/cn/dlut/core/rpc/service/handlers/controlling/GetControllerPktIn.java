package cn.dlut.core.rpc.service.handlers.controlling;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import cn.dlut.core.main.CoordinatorQueue;
import cn.dlut.core.rpc.service.handlers.ApiHandler;
import cn.dlut.elements.controller.Controller;

public class GetControllerPktIn extends ApiHandler<Map<String, String>> {

	Logger log = LogManager.getLogger(GetControllerPktIn.class.getName());
	@Override
	public JSONRPC2Response process(final Map<String, String> params) {
		JSONRPC2Response resp = null;
		String controllerIp = params.get("controllerIp");
		String controllerPort = params.get("controllerPort");
		
		String responseString = "no controller match!";
		//find this controller in CoordinatorQueue
    	for (Controller c : CoordinatorQueue.getCtrlPktIn().keySet()) {
    		if(c.getIp().equals(controllerIp) && c.getPort().toString().equals(controllerPort)) {
    			this.log.info("get packet in queue length");
    			responseString = "queue length: " + CoordinatorQueue.getCtrlPktIn().get(c).size();
    			break;
    		}
    	}
    	resp = new JSONRPC2Response(responseString, 0);
		//resp.setResult(JSONObject.fromObject(Map jsonresponse));
		return resp;
	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.NO_PARAMS;
	}

	
}
