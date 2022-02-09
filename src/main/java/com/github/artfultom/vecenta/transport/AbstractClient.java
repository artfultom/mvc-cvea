package com.github.artfultom.vecenta.transport;

import com.github.artfultom.vecenta.matcher.ReadWriteStrategy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractClient implements Client {

    protected ReadWriteStrategy strategy;

    protected void handshake(DataInputStream in, DataOutputStream out) throws IOException {
        ByteBuffer protocolInfo = ByteBuffer.allocate(9);
        protocolInfo.put(AbstractServer.PROTOCOL_NAME.getBytes());
        protocolInfo.putInt(AbstractServer.PROTOCOL_VERSION);

        protocolInfo.put((byte) 0b11101010);    // TODO handshake logic

        out.writeInt(protocolInfo.capacity());
        out.write(protocolInfo.array());
        out.flush();

        int size = in.readInt();
        ByteBuffer bb = ByteBuffer.wrap(in.readNBytes(size));
        int result = bb.asIntBuffer().get();

        // TODO process
    }
}
