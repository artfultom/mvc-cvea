package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.matcher.ReadWriteStrategy;
import io.github.artfultom.vecenta.transport.error.MessageError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public abstract class AbstractConnector implements Connector {

    private static final Logger log = LoggerFactory.getLogger(AbstractConnector.class);
    protected ReadWriteStrategy strategy;

    protected synchronized void handshake(MessageStream stream) {
        int capacity = 0;
        capacity += AbstractServer.PROTOCOL_NAME.getBytes().length;
        capacity += Integer.BYTES;

        ByteBuffer protocolInfo = ByteBuffer.allocate(capacity);
        protocolInfo.put(AbstractServer.PROTOCOL_NAME.getBytes());
        protocolInfo.putInt(AbstractServer.PROTOCOL_VERSION);

        stream.sendMessage(protocolInfo.array());

        ByteBuffer bb = ByteBuffer.wrap(stream.getMessage());
        int result = bb.asIntBuffer().get();

        if (result != 0) {
            log.error(String.format("Handshake error: %s", MessageError.get(result)));  // TODO ProtocolException
        }
    }
}
