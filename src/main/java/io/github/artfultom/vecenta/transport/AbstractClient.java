package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.matcher.ReadWriteStrategy;
import io.github.artfultom.vecenta.transport.error.MessageError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractClient implements Client {

    private static final Logger log = LoggerFactory.getLogger(AbstractClient.class);
    protected ReadWriteStrategy strategy;

    protected void handshake(DataInputStream in, DataOutputStream out) throws IOException {
        int capacity = 0;
        capacity += AbstractServer.PROTOCOL_NAME.getBytes().length;
        capacity += Integer.BYTES;

        ByteBuffer protocolInfo = ByteBuffer.allocate(capacity);
        protocolInfo.put(AbstractServer.PROTOCOL_NAME.getBytes());
        protocolInfo.putInt(AbstractServer.PROTOCOL_VERSION);

        out.writeInt(protocolInfo.capacity());
        out.write(protocolInfo.array());
        out.flush();

        int size = in.readInt();
        ByteBuffer bb = ByteBuffer.wrap(in.readNBytes(size));
        int result = bb.asIntBuffer().get();

        if (result != 0) {
            log.error("Handshake error: " + MessageError.get(result));
        }
    }
}
