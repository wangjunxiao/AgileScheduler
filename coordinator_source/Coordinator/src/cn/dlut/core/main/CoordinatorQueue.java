package cn.dlut.core.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import cn.dlut.elements.controller.Controller;
import cn.dlut.elements.datapath.VirtualSwitch;
import cn.dlut.elements.network.PhysicalNetwork;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoordinatorQueue {
	
	public static Logger log = LogManager.getLogger(CoordinatorQueue.class.getName());
	
	/*
	 * 
	 * update operation for AtomicReference Object 
	 * need to clone first and then reset it to finish update
	 * 
	 * */
	
	/*mapping of Controller and OFPacketInList*/
	// ArrayList<Integer>--bufferId
	private static AtomicReference<HashMap<Controller, ArrayList<Integer>>> ctrl_pktins = 
			new AtomicReference<HashMap<Controller, ArrayList<Integer>>>();
	
	@SuppressWarnings("unchecked")
	public static void  initCtrlPktIn(Controller ctrl){
		HashMap<Controller, ArrayList<Integer>> map = 
				(HashMap<Controller, ArrayList<Integer>>) getCtrlPktIn().clone();
		map.put(ctrl, new ArrayList<Integer>());
 		setCtrlPktIn(map);
	}
	
	/*push OFPacketIn for specified Controller*/
	@SuppressWarnings("unchecked")
	public static void pushCtrlPktIn(Controller ctrl, Integer bufferid) {
		HashMap<Controller, ArrayList<Integer>> map = (HashMap<Controller, ArrayList<Integer>>) getCtrlPktIn().clone();
		ArrayList<Integer> list = map.get(ctrl);
		list.add(bufferid);
		map.put(ctrl, list);
		setCtrlPktIn(map);
	}
	
	/*pop OFPacketIn for specified Controller*/
	@SuppressWarnings("unchecked")
	public static void popCtrlPktIn(Controller ctrl, Integer bufferid) {
		HashMap<Controller, ArrayList<Integer>> map = 
				(HashMap<Controller, ArrayList<Integer>>) getCtrlPktIn().clone();
		ArrayList<Integer> list = map.get(ctrl);
		if (list.size() > 0) {			
			list.remove(0);
		}
		map.put(ctrl, list);
		setCtrlPktIn(map);
	}
	
	@SuppressWarnings("unchecked")
	public static void clearCtrlPktIn(Controller ctrl) {
		HashMap<Controller, ArrayList<Integer>> map = 
				(HashMap<Controller, ArrayList<Integer>>) getCtrlPktIn().clone();
		map.remove(ctrl);
		setCtrlPktIn(map);
	}
	
	private static void setCtrlPktIn(HashMap<Controller, ArrayList<Integer>> map) {
		ctrl_pktins.set(map);
	}
	
	public static HashMap<Controller, ArrayList<Integer>> getCtrlPktIn() {
		if(ctrl_pktins.get() == null) {
			HashMap<Controller, ArrayList<Integer>> map  = new HashMap<Controller, ArrayList<Integer>>();
			setCtrlPktIn(map);
		}
		return ctrl_pktins.get();
	}
	
	public static Controller getMinQueueController(Long switchID) {
		Integer min = Integer.MAX_VALUE;
		Controller ctrl = null;
		for(VirtualSwitch virsw : PhysicalNetwork.getInstance().getSwitch(switchID).getVirtualSwitchSet()) {
			if (getCtrlPktIn().get(virsw.getController()).size() < min) {
				ctrl = virsw.getController();
				min = getCtrlPktIn().get(virsw.getController()).size();
			}
		}
		return ctrl;
	}
	
}
