package cn.dlut.elements.controller;

import java.util.ArrayList;
import java.util.List;

import cn.dlut.elements.datapath.VirtualSwitch;


public class Controller {
	
	private String ip = null;
	private Integer port = null;
	private List<VirtualSwitch> vsw = null;
	
	public Controller(final String ipAddress, final Integer port) {
        this.ip = ipAddress;
        this.port = port;
        this.vsw = new ArrayList<VirtualSwitch>();
    }
	public Controller()
	{
		this.ip=null;
		this.port=null;
	}
	
	public String toSimpleString() {
        return ip+":"+port;
    }
	
	public void setIp(final String ipAddress){
		this.ip = ipAddress;
	}
	
	public void setPort(final Integer port){
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public Integer getPort() {
		return port;
	}
		
	@Override
	public String toString()
	{
		return this.getIp()+":"+this.getPort();
	}
	public List<VirtualSwitch> getVirtualSwitch() {
		return vsw;
	}
	public void addVirtualSwitch(VirtualSwitch vswitch) {
		this.vsw.add(vswitch);
	}

}