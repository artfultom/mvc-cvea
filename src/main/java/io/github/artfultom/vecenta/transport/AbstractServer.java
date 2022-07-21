package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.transport.error.MessageError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class AbstractServer implements Server {

    private static final Logger log = LoggerFactory.getLogger(AbstractServer.class);

    public static final int PROTOCOL_VERSION = 1;
    public static final String PROTOCOL_NAME = "vcea";

    protected void handshake(MessageStream stream) throws ConnectionException {
        try {
            byte[] handshake = stream.getMessage();

            if (handshake.length > 4) {
                byte[] protocolNameArr = new byte[PROTOCOL_NAME.length()];
                ByteBuffer buf = ByteBuffer.wrap(handshake);
                buf.get(protocolNameArr);

                if (PROTOCOL_NAME.equals(new String(protocolNameArr, StandardCharsets.UTF_8))) {
                    int protocolVersion = buf.getInt();

                    ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
                    if (protocolVersion == PROTOCOL_VERSION) {
                        bb.putInt(0);

                        stream.sendMessage(bb.array());
                    } else {
                        bb.putInt(MessageError.WRONG_PROTOCOL_VERSION.ordinal());

                        stream.sendMessage(bb.array());

                        log.error(String.format("Wrong protocol version: %d", protocolVersion));    // TODO exception
                    }

                    return;
                }
            }

            ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
            bb.putInt(MessageError.WRONG_PROTOCOL.ordinal());

            stream.sendMessage(bb.array());
        } catch (IOException e) {
            throw new ConnectionException("Cannot handshake (server).", e);
        }
    }
}
