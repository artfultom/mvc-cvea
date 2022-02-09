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
                    // TODO handshake process

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
}
