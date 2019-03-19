package cn.dlut.core.io;

import java.io.IOException;

import cn.dlut.core.main.Coordinator;
import cn.dlut.elements.datapath.Switch;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.openflow.protocol.OFType;

public abstract class OFChannelHandler extends IdleStateAwareChannelHandler {

    @SuppressWarnings("rawtypes")
    protected Switch sw;
    protected Channel channel;
    protected Coordinator ctrl;

    public abstract boolean isHandShakeComplete();

    protected abstract String getSwitchInfoString();

    protected abstract void sendHandShakeMessage(OFType type)
            throws IOException;

}
