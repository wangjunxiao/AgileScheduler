package cn.dlut.core.io;

import java.util.concurrent.ThreadPoolExecutor;

import cn.dlut.elements.network.PhysicalNetwork;
import cn.dlut.core.io.SwitchChannelHandler;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;

public class SwitchChannelPipeline extends OpenflowChannelPipeline {

    private ExecutionHandler eh = null;

    public SwitchChannelPipeline(
            final ThreadPoolExecutor pipelineExecutor) {
        super();
        this.pipelineExecutor = pipelineExecutor;
        this.timer = PhysicalNetwork.getTimer();
        this.idleHandler = new IdleStateHandler(this.timer, 20, 25, 0);
        this.readTimeoutHandler = new ReadTimeoutHandler(this.timer, 30);
        this.eh = new ExecutionHandler(this.pipelineExecutor);
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        final SwitchChannelHandler handler = new SwitchChannelHandler();
        final ChannelPipeline pipeline = Channels.pipeline();
        
        pipeline.addLast("ofmessagedecoder", new CoordinatorMessageDecoder());
        pipeline.addLast("ofmessageencoder", new CoordinatorMessageEncoder());
        pipeline.addLast("idle", this.idleHandler);
        pipeline.addLast("timeout", this.readTimeoutHandler);
        pipeline.addLast("handshaketimeout", new HandshakeTimeoutHandler(handler, this.timer, 15));
        pipeline.addLast("pipelineExecutor", eh);
        pipeline.addLast("handler", handler);
        return pipeline;
    }

}
