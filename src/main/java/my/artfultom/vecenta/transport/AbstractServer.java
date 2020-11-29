package my.artfultom.vecenta.transport;

import my.artfultom.vecenta.transport.error.HandshakeError;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class AbstractServer implements Server {

    public static final int PROTOCOL_VERSION = 1;

    protected void handshake(MessageStream stream) {
        byte[] handshake = stream.getNextMessage();
        // TODO handshake process

        if (handshake.length > 4) {
            byte[] protocolNameArr = new byte[4];
            ByteBuffer.wrap(handshake).get(protocolNameArr);
            if ("vcea".equals(new String(protocolNameArr, StandardCharsets.UTF_8))) {
                int protocolVersion = handshake[4];

                if (protocolVersion == PROTOCOL_VERSION) {
                    ByteBuffer bb = ByteBuffer.allocate(4);
                    bb.putInt(0);

                    stream.sendMessage(bb.array());
                } else {
                    ByteBuffer bb = ByteBuffer.allocate(4);
                    bb.putInt(HandshakeError.WRONG_PROTOCOL_VERSION.ordinal());

                    stream.sendMessage(bb.array());
                }

                return;
            }
        }

        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(HandshakeError.WRONG_PROTOCOL.ordinal());

        stream.sendMessage(bb.array());
    }
}
