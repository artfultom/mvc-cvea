package io.github.artfultom.vecenta.transport;

import java.io.IOException;

public interface MessageStream extends AutoCloseable {

    byte[] getMessage() throws IOException;

    void sendMessage(byte[] resp) throws IOException;

    @Override
    void close() throws IOException;
}
