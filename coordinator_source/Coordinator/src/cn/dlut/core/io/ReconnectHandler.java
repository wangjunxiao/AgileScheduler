package cn.dlut.core.io;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import cn.dlut.elements.datapath.VirtualSwitch;
import cn.dlut.exceptions.ReconnectException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

public class ReconnectHandler extends SimpleChannelHandler {

    Logger log = LogManager.getLogger(ReconnectHandler.class.getName());

    static final ReconnectException EXCEPTION = new ReconnectException();

    final ClientBootstrap bootstrap;
    final Timer timer;
    volatile Timeout timeout;
    private final Integer maxBackOff;
    private final VirtualSwitch virsw;
    private final ChannelGroup cg;

    public ReconnectHandler(final VirtualSwitch virsw,
            final ClientBootstrap bootstrap, final Timer timer,
            final int maxBackOff, final ChannelGroup cg) {
        super();
        this.virsw = virsw;
        this.bootstrap = bootstrap;
        this.timer = timer;
        this.maxBackOff = maxBackOff;
        this.cg = cg;
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx,
            final ChannelStateEvent e) {
        if (this.virsw.isConnected()) {
            return;
        }
        final int retry = this.virsw.incrementBackOff();
        final Integer backOffTime = Math.min(2 << (retry + 1), this.maxBackOff);
        this.timeout = this.timer.newTimeout(new ReconnectTimeoutTask(this.virsw,
                this.cg), backOffTime, TimeUnit.SECONDS);

        this.log.error("Backing off {} for controller {}", backOffTime,
                this.bootstrap.getOption("remoteAddress"));
        ctx.sendUpstream(e);
    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx,
            final ChannelStateEvent e) {
        ctx.sendUpstream(e);
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx,
            final ChannelStateEvent e) {
        this.virsw.resetBackOff();
        ctx.sendUpstream(e);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx,
            final ExceptionEvent e) {

        final Throwable cause = e.getCause();
        if (cause instanceof ConnectException) {
            return;
        }
        ctx.sendUpstream(e);
    }

    private final class ReconnectTimeoutTask implements TimerTask {

    	VirtualSwitch virsw = null;
        private final ChannelGroup cg;

        public ReconnectTimeoutTask(final VirtualSwitch virsw, final ChannelGroup cg) {
            this.virsw = virsw;
            this.cg = cg;
        }

        @Override
        public void run(final Timeout timeout) throws Exception {
        	final InetSocketAddress remoteAddr = (InetSocketAddress) ReconnectHandler.this.bootstrap
                    .getOption("remoteAddress");
        	//establish netty channel
            final ChannelFuture cf = bootstrap.connect();
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture e)
                        throws Exception {
                    if (e.isSuccess()) {
                    	ReconnectTimeoutTask.this.virsw.setChannel(e.getChannel());
                    	ReconnectTimeoutTask.this.cg.add(e.getChannel());
                    }
                    else {
                    	ReconnectHandler.this.log
                             .error("Failed to connect to controller {} for switch {}",
                                   remoteAddr, ReconnectTimeoutTask.this.virsw.getSwitchName());
                    }
                }
            });
        }
    }
}
