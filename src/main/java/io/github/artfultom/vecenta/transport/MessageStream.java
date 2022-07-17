package io.github.artfultom.vecenta.transport;

import java.io.IOException;

public interface MessageStream extends AutoCloseable {

    byte[] getMessage();

    void sendMessage(byte[] resp);

    @Override
    void close() throws IOException;
}
