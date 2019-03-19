package cn.dlut;

import java.util.HashMap;

import cn.dlut.elements.controller.Controller;

public class Test {
	
	public String test(){
		return "success";
	}
	
	/*mapping of Controller and OFPacketInList <Integer,Integer> <Key, BufferID>*/
	protected static HashMap<Controller, HashMap<Integer, Integer>> ctrl_pktouts = 
			new HashMap<Controller, HashMap<Integer, Integer>>();
	
	protected static HashMap<Controller, HashMap<Integer, Integer>> ctrl_flowmods = 
			new HashMap<Controller, HashMap<Integer, Integer>>();
	
	public static void main(String[] args){
		double start = System.currentTimeMillis() ;	

		for(int ii=0; ii<3; ii++){
			//int queue[] = new int[100000];
		}
		double end = System.currentTimeMillis() ;
		System.out.println("time is : " + (end - start));
	}
	
}
