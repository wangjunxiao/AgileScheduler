package cn.dlut.core.rpc.service.handlers.controlling;

import java.util.Map;

import cn.dlut.core.rpc.service.handlers.ApiHandler;
import cn.dlut.core.main.Coordinator;
import cn.dlut.core.main.CoordinatorQueue;
import cn.dlut.elements.controller.Controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class DeleteController extends ApiHandler<Map<String, String>> {
	
	Logger log=LogManager.getLogger(DeleteController.class.getName());
	
	@Override
	public JSONRPC2Response process(final Map<String, String> params) {
		JSONRPC2Response resp = null;
		//this.log.info("receive request");
		String controllerIp = params.get("controllerIp");
		String controllerPort = params.get("controllerPort");
		
		String responseString = "no controller match!";
		//find this controller in CoordinatorQueue
    	for (Controller c : CoordinatorQueue.getCtrlPktIn().keySet()) {
    		if(c.getIp().equals(controllerIp) && c.getPort().toString().equals(controllerPort)) {
    			Coordinator.getInstance().handleControllerDeleted(c);
    			this.log.info("deleting controllers..." );
    			responseString = "delete controller success!";
    			break;
    		}
    	}
    	resp = new JSONRPC2Response(responseString, 0); 
    	return resp;
	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}
