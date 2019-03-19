package cn.dlut.core.io;

import java.util.concurrent.ThreadPoolExecutor;

import cn.dlut.core.main.Coordinator;
import cn.dlut.elements.network.PhysicalNetwork;
import cn.dlut.core.io.ControllerChannelHandler;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;

import cn.dlut.elements.datapath.VirtualSwitch;

public class ClientChannelPipeline extends OpenflowChannelPipeline {

    private ClientBootstrap bootstrap = null;
    private VirtualSwitch virsw = null;
    private final ChannelGroup cg;

    public ClientChannelPipeline(
            final Coordinator Coordinator,
            final ChannelGroup cg, final ThreadPoolExecutor pipelineExecutor,
            final ClientBootstrap bootstrap, final VirtualSwitch virsw) {
        super();
        this.ctrl = Coordinator;
        this.pipelineExecutor = pipelineExecutor;
        this.timer = PhysicalNetwork.getTimer();
        this.idleHandler = new IdleStateHandler(this.timer, 20, 25, 0);
        this.readTimeoutHandler = new ReadTimeoutHandler(this.timer, 30);
        this.bootstrap = bootstrap;
        this.virsw = virsw;
        this.cg = cg;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        final ControllerChannelHandler handler = new ControllerChannelHandler(this.virsw);
        
        final ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("reconnect", new ReconnectHandler(this.virsw,
                this.bootstrap, this.timer, 30, this.cg));
        pipeline.addLast("ofmessagedecoder", new CoordinatorMessageDecoder());
        pipeline.addLast("ofmessageencoder", new CoordinatorMessageEncoder());
        pipeline.addLast("idle", this.idleHandler);
        pipeline.addLast("timeout", this.readTimeoutHandler);
        pipeline.addLast("handshaketimeout", new HandshakeTimeoutHandler(
                handler, this.timer, 15));
        pipeline.addLast("pipelineExecutor", new ExecutionHandler(
                this.pipelineExecutor));
        pipeline.addLast("handler", handler);
        return pipeline;
    }
    
}
