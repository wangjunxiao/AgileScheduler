package cn.dlut.core.io;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.factory.BasicFactory;

/**
 * Decode an openflow message from a netty Channel.
 *
 * @author alshabib
 */
public class CoordinatorMessageDecoder extends FrameDecoder {

    BasicFactory factory = BasicFactory.getInstance();

    @Override
    protected Object decode(final ChannelHandlerContext ctx,
            final Channel channel, final ChannelBuffer buffer) throws Exception {
        if (!channel.isConnected()) {
            // if the channel is closed, there will be nothing to read.
            return null;
        }

        final List<OFMessage> message = this.factory.parseMessage(buffer);
        return message;
    }

}
