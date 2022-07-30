package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.transport.error.ErrorType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class AbstractServer implements Server {

    public static final int PROTOCOL_VERSION = 1;

    public static final String PROTOCOL_NAME = "PSRP";   // Pretty Simple RPC Protocol

    protected MessageHandler getHandler;

    protected MessageHandler sendHandler;

    @Override
    public void setGetHandler(MessageHandler handler) {
        this.getHandler = handler;
    }

    @Override
    public void setSendHandler(MessageHandler handler) {
        this.sendHandler = handler;
    }

    protected void handshake(MessageStream stream) throws ConnectionException, ProtocolException {
        try {
            byte[] handshake = stream.getMessage();

            if (handshake.length > PROTOCOL_NAME.length()) {
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
                        bb.putInt(ErrorType.WRONG_PROTOCOL_VERSION.ordinal());
                        stream.sendMessage(bb.array());
                        throw new ProtocolException(ErrorType.WRONG_PROTOCOL);
                    }

                    return;
                }
            }

            ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
            bb.putInt(ErrorType.WRONG_PROTOCOL.ordinal());

            stream.sendMessage(bb.array());
        } catch (IOException e) {
            throw new ConnectionException("Cannot handshake (server).", e);
        }
    }
}
