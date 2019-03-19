package cn.dlut.packet;

import java.nio.ByteBuffer;


/**
 * This class represents an Link Local Control header that is used in Ethernet
 * 802.3.
 *
 * @author alexreimers
 *
 */
public class LLC extends BasePacket {
    private byte dsap = 0;
    private byte ssap = 0;
    private byte ctrl = 0;

    public byte getDsap() {
        return this.dsap;
    }

    public void setDsap(final byte dsap) {
        this.dsap = dsap;
    }

    public byte getSsap() {
        return this.ssap;
    }

    public void setSsap(final byte ssap) {
        this.ssap = ssap;
    }

    public byte getCtrl() {
        return this.ctrl;
    }

    public void setCtrl(final byte ctrl) {
        this.ctrl = ctrl;
    }
    
    @Override
    public byte[] serialize() {
        final byte[] data = new byte[3];
        final ByteBuffer bb = ByteBuffer.wrap(data);
        bb.put(this.dsap);
        bb.put(this.ssap);
        bb.put(this.ctrl);
        return data;
    }

    @Override
    public IPacket deserialize(final byte[] data, final int offset,
            final int length) {
        final ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
        this.dsap = bb.get();
        this.ssap = bb.get();
        this.ctrl = bb.get();
        return this;
    }

}
