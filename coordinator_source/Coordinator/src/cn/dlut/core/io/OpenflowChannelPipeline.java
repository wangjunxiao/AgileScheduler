package cn.dlut.core.io;

import java.util.concurrent.ThreadPoolExecutor;

import cn.dlut.core.main.Coordinator;

import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.jboss.netty.util.Timer;

public abstract class OpenflowChannelPipeline implements
        ChannelPipelineFactory, ExternalResourceReleasable {
    protected Coordinator ctrl;
    protected ThreadPoolExecutor pipelineExecutor;
    protected Timer timer;
    protected IdleStateHandler idleHandler;
    protected ReadTimeoutHandler readTimeoutHandler;

    @Override
    public void releaseExternalResources() {
        this.timer.stop();
    }
}
