package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.transport.error.MessageError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class AbstractServer implements Server {

    private static final Logger log = LoggerFactory.getLogger(AbstractServer.class);

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
                    ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
                    bb.putInt(0);

                    stream.sendMessage(bb.array());
                } else {
                    ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
                    bb.putInt(MessageError.WRONG_PROTOCOL_VERSION.ordinal());

                    stream.sendMessage(bb.array());

                    log.error("Wrong protocol version: " + protocolVersion);
                }

                return;
            }
        }

        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        bb.putInt(MessageError.WRONG_PROTOCOL.ordinal());

        stream.sendMessage(bb.array());
    }
}
