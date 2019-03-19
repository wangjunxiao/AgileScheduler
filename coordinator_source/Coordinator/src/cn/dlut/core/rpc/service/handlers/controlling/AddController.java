package cn.dlut.core.rpc.service.handlers.controlling;

import java.util.Map;

import cn.dlut.core.rpc.service.handlers.ApiHandler;
import cn.dlut.core.main.Coordinator;
import cn.dlut.elements.controller.Controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class AddController extends ApiHandler<Map<String, String>> {

    Logger log = LogManager.getLogger(AddController.class.getName());

    @Override
	public JSONRPC2Response process(final Map<String, String> params) {
    	JSONRPC2Response resp = null;
		String controllerIp = params.get("controllerIp");
		String controllerPort = params.get("controllerPort");
		Controller ctrl = new Controller(controllerIp, Integer.parseInt(controllerPort));
		Coordinator.getInstance().handleControllerAdded(ctrl);
		this.log.info("add controllers..." );
		resp = new JSONRPC2Response("add controller success!", 0);
	    return resp;
	}

    @Override
    public JSONRPC2ParamsType getType() {
        return JSONRPC2ParamsType.OBJECT;
    }

}
