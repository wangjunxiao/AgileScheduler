package cn.dlut.core.io;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.openflow.protocol.OFMessage;

/**
 * encode an openflow message into a netty Channel.
 *
 */
public class CoordinatorMessageEncoder extends OneToOneEncoder {

    @Override
    protected Object encode(final ChannelHandlerContext ctx,
            final Channel channel, final Object msg) throws Exception {
        if (!(msg instanceof List)) {
            return msg;
        }

        @SuppressWarnings("unchecked")
        final List<OFMessage> msglist = (List<OFMessage>) msg;
        int size = 0;
        for (final OFMessage ofm : msglist) {
            size += ofm.getLengthU();
        }

        final ChannelBuffer buf = ChannelBuffers.buffer(size);

        for (final OFMessage ofm : msglist) {

            ofm.writeTo(buf);

        }
        return buf;
    }

}
