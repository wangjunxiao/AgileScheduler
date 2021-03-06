package cn.dlut.core.main;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.openflow.vendor.nicira.OFNiciraVendorExtensions;

import cn.dlut.core.cmd.CmdLineSettings;
import cn.dlut.core.io.ClientChannelPipeline;
import cn.dlut.core.io.SwitchChannelPipeline;
import cn.dlut.core.rpc.server.JettyServer;
import cn.dlut.elements.controller.Controller;
import cn.dlut.elements.datapath.VirtualSwitch;
import cn.dlut.elements.datapath.PhysicalSwitch;
import cn.dlut.elements.network.PhysicalNetwork;

public class Coordinator implements Runnable {

    Logger log = LogManager.getLogger(Coordinator.class.getName());
    private static Coordinator instance = null;
    private static final int SEND_BUFFER_SIZE = 1024 * 1024;
    
    private String ofHost = null;
    private Integer ofPort = null;
    private Integer statsRefresh = null;
    private Boolean useBDDP = null;
    
    private NioClientSocketChannelFactory clientSockets = null;
    private ThreadPoolExecutor clientThreads = null;
    private ThreadPoolExecutor serverThreads = null;
    private ChannelGroup sg = null;
    private ChannelGroup cg = null;
    //of message channel works between southbound underlying switch and coordinator physical switch
    private SwitchChannelPipeline pfact = null;
    //of message channel works between northbound controller and coordinator virtual switch
    private ClientChannelPipeline cfact = null;
    private Integer nClientThreads = null;
    private Integer nServerThreads = null;
    //jetty rpc server
    private Thread rpcserver = null;
    //configure default type of replica selection algorithm
    public static Integer coordinator_arithmetic = 1;
 
    
    public Coordinator(CmdLineSettings settings) {
    	Coordinator.instance = this;
        this.ofHost = settings.getOFHost();
        this.ofPort = settings.getOFPort();
        this.log.info("Coordinator is running at {}:{}", ofHost, ofPort);
        this.statsRefresh = settings.getStatsRefresh();
        this.useBDDP = settings.getUseBDDP();
        this.clientSockets = new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        this.nClientThreads = settings.getClientThreads();
        this.nServerThreads = settings.getServerThreads();
        this.clientThreads = new OrderedMemoryAwareThreadPoolExecutor(nClientThreads, 1048576, 1048576, 5, TimeUnit.SECONDS);
        this.serverThreads = new OrderedMemoryAwareThreadPoolExecutor(nServerThreads, 1048576, 1048576, 5, TimeUnit.SECONDS);
        this.sg = new DefaultChannelGroup();
        this.cg = new DefaultChannelGroup();
        this.pfact = new SwitchChannelPipeline(this.serverThreads);
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new CoordinatorShutdownHook(this));
        initVendorMessages();
        PhysicalNetwork.getInstance().boot();
        this.startRPCServer();
        /* startup southbound of message channel for interacting with southbound underlying switches --start-- */
        try {
            final ServerBootstrap switchServerBootStrap = this.createServerBootStrap();
            this.setServerBootStrapParams(switchServerBootStrap);
            switchServerBootStrap.setPipelineFactory(this.pfact);
            final InetSocketAddress sa = this.ofHost == null ? 
            		new InetSocketAddress(this.ofPort) : 
            		new InetSocketAddress(this.ofHost, this.ofPort);
            this.sg.add(switchServerBootStrap.bind(sa));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        /* startup southbound ofmessage channel for interacting with southbound underlying switches --end-- */
    }
    
    private void connectCtrlandPsw (Controller ctrl, PhysicalSwitch psw) {
    	VirtualSwitch virsw = new VirtualSwitch(psw.getSwitchId());
    	psw.addVirtualSwitchSet(virsw);
    	virsw.setPhysicalSwitch(psw);
    	ctrl.addVirtualSwitch(virsw);
    	virsw.setController(ctrl);
    	virsw.boot();
    
        /* startup northbound of message channel for interacting with northbound controller --start-- */
        final ClientBootstrap clientBootStrap = this
                .createClientBootStrap();
        this.setClientBootStrapParams(clientBootStrap);
        String ipAddress = ctrl.getIp();
        Integer port = ctrl.getPort();
        final InetSocketAddress remoteAddr = new InetSocketAddress(ipAddress, port);
        clientBootStrap.setOption("remoteAddress", remoteAddr);
        this.cfact = new ClientChannelPipeline(this, this.cg,
                this.clientThreads, clientBootStrap, virsw);
        clientBootStrap.setPipelineFactory(this.cfact);
        //establish netty channel
        final ChannelFuture cf = clientBootStrap.connect();
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture e)
                    throws Exception {
                if (e.isSuccess()) {
                    virsw.setChannel(e.getChannel());
                    cg.add(e.getChannel());
                } else {
                    //e.getChannel().getCloseFuture().awaitUninterruptibly();
                    Coordinator.this.log.error("Failed to connect to controller {} for switch {}",
                                    remoteAddr, virsw.getSwitchName());
                }
            }
        });
        /* startup northbound of message channel for interacting with northbound controller --end-- */
    }
    
    //call by switchchannelhandler
    public void handleSwitchConnected(PhysicalSwitch psw) {
    	//create corresponding virtual switches and ofmessage channel for each controller connected with this physical switch  
    	for (Controller ctrl : CoordinatorQueue.getCtrlPktIn().keySet()) {
	    	this.connectCtrlandPsw(ctrl, psw);
    	}
    	// TODO : need to fix bug
    	/*  NOTE this case: when you firstly connect southbound switch with Coordinator, and then connect Coordinator with 
    	 *	northbound controller, it works well. But when you change this order, such as firstly add controller and then 
    	 *  connect southbound switch, it will be in trouble.
    	 **/
    }
    
    //call by rpcAddController
    public void handleControllerAdded(Controller ctrl) { // NOTE this controller has an empty VirtualSwitchSet 
    	//create corresponding virtual switches and ofmessage channel for each controller connected with this physical switch 
    	for(PhysicalSwitch psw : PhysicalNetwork.getInstance().getSwitches()) {
    		this.connectCtrlandPsw(ctrl, psw);
	    }
    	//register Coordinator Queue
    	CoordinatorQueue.initCtrlPktIn(ctrl);
    }
    
    //call by rpcDeleteController
    public void handleControllerDeleted(Controller ctrl) {
    	//turnoff ofmessage channel and clear corresponding mapping
    	while (!ctrl.getVirtualSwitch().isEmpty()) {
    		ctrl.getVirtualSwitch().get(0).unregister();
    	}
    	//unregister Coordinator Queue
    	CoordinatorQueue.clearCtrlPktIn(ctrl);
    }
    
    private void setServerBootStrapParams(final ServerBootstrap bootstrap) {
        bootstrap.setOption("reuseAddr", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.sendBufferSize", Coordinator.SEND_BUFFER_SIZE);
    }

    private void setClientBootStrapParams(final ClientBootstrap bootstrap) {
        bootstrap.setOption("reuseAddr", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.sendBufferSize", Coordinator.SEND_BUFFER_SIZE);
    }

    private ClientBootstrap createClientBootStrap() {
        return new ClientBootstrap(this.clientSockets);
    }

    private ServerBootstrap createServerBootStrap() {
        return new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
    }

    public void terminate() {
        if (this.cg != null && this.cg.close().awaitUninterruptibly(1000)) {
            this.log.info("Shut down all controller connections. Quitting...");
        } else {
            this.log.error("Error shutting down all controller connections. Quitting anyway.");
        }
        if (this.sg != null && this.sg.close().awaitUninterruptibly(1000)) {
            this.log.info("Shut down all switch connections. Quitting...");
        } else {
            this.log.error("Error shutting down all switch connections. Quitting anyway.");
        }
        if (this.pfact != null) {
            this.pfact.releaseExternalResources();
        }
        if (this.cfact != null) {
            this.cfact.releaseExternalResources();
        }
    }
    
    public static boolean containsCtrl(Controller ctrl, Set<Controller> ctrls){
    	if(ctrls.isEmpty())
    		return false;
    	for(Controller c : ctrls){
    		if(c.getIp().equals(ctrl.getIp()) && c.getPort().equals(ctrl.getPort()))
    			return true;
    	}
    	return false;
    }

    public static Coordinator getInstance() {
        if (Coordinator.instance == null) {
            throw new RuntimeException("The Coordinator has not been initialized; quitting.");
        }
        return Coordinator.instance;
    }

    public Integer getStatsRefresh() {
        return this.statsRefresh;
    }

    private void initVendorMessages() {
        // Configure openflowj to be able to parse the role request/reply vendor messages.
        OFNiciraVendorExtensions.initialize();
    }
    
    private void startRPCServer() {
        this.rpcserver = new Thread(new JettyServer(8080));
        this.rpcserver.start();
    }

    public Boolean getUseBDDP() {
        return this.useBDDP;
    }
    
    public static void setCoordinator_arithmetic(int ath){
    	Coordinator.coordinator_arithmetic=ath;
    }
    
    public static int getCoordinatorArithmetic()
    {
 	   return Coordinator.coordinator_arithmetic;
    }
    
}
