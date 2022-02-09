package com.github.artfultom.vecenta.transport;

import com.github.artfultom.vecenta.transport.error.MessageError;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class AbstractServer implements Server {

    public static final int PROTOCOL_VERSION = 1;
    public static final String PROTOCOL_NAME = "vcea";

    protected void handshake(MessageStream stream) {
        byte[] handshake = stream.getNextMessage();

        if (handshake.length > 4) {
            byte[] protocolNameArr = new byte[PROTOCOL_NAME.length()];
            ByteBuffer buf = ByteBuffer.wrap(handshake);
            buf.get(protocolNameArr);

            if (PROTOCOL_NAME.equals(new String(protocolNameArr, StandardCharsets.UTF_8))) {
                int protocolVersion = buf.getInt();

                if (protocolVersion == PROTOCOL_VERSION) {
                    byte flags = buf.get();
                    setFlags(flags);

                    ByteBuffer bb = ByteBuffer.allocate(4);
                    bb.putInt(0);

                    stream.sendMessage(bb.array());
                } else {
                    ByteBuffer bb = ByteBuffer.allocate(4);
                    bb.putInt(MessageError.WRONG_PROTOCOL_VERSION.ordinal());

                    stream.sendMessage(bb.array());
                }

                return;
            }
        }

        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(MessageError.WRONG_PROTOCOL.ordinal());

        stream.sendMessage(bb.array());
    }

    private void setFlags(byte flags) {
        boolean[] bs = new boolean[8];
        bs[0] = (flags & 0x80) != 0;
        bs[1] = (flags & 0x40) != 0;
        bs[2] = (flags & 0x20) != 0;
        bs[3] = (flags & 0x10) != 0;
        bs[4] = (flags & 0x8) != 0;
        bs[5] = (flags & 0x4) != 0;
        bs[6] = (flags & 0x2) != 0;
        bs[7] = (flags & 0x1) != 0;

        // TODO handshake logic
    }
}
