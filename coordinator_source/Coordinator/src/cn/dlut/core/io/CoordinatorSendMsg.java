package cn.dlut.core.io;

import org.openflow.protocol.OFMessage;

public interface CoordinatorSendMsg {
    public void sendMsg(OFMessage msg);

    public String getName();
}
