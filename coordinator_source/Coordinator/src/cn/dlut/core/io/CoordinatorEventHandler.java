package cn.dlut.core.io;

import org.jboss.netty.channel.Channel;
import org.openflow.protocol.OFMessage;

public interface CoordinatorEventHandler {

    public void handleIO(OFMessage msg, Channel channel);
    
}
