package com.github.artfultom.vecenta.transport;

import com.github.artfultom.vecenta.matcher.ReadWriteStrategy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractClient implements Client {

    protected ReadWriteStrategy strategy;

    protected void handshake(DataInputStream in, DataOutputStream out) throws IOException {
        out.writeInt(5);
        out.write("vcea".getBytes());
        out.write(AbstractServer.PROTOCOL_VERSION);
        out.flush();

        int size = in.readInt();
        ByteBuffer bb = ByteBuffer.wrap(in.readNBytes(size));
        int result = bb.asIntBuffer().get();

        // TODO handshake logic
    }
}
