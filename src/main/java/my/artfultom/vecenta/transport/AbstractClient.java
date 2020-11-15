package my.artfultom.vecenta.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractClient implements Client {

    protected void handshake(DataInputStream in, DataOutputStream out) throws IOException {
        out.writeInt(1);
        out.write(2);
        out.flush();

        int size = in.readInt();
        byte[] result = in.readNBytes(size);
        // TODO handshake process
    }
}
