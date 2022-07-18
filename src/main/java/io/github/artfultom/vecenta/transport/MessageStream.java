package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.exceptions.ConnectionException;

import java.io.IOException;

public interface MessageStream extends AutoCloseable {

    byte[] getMessage() throws ConnectionException;

    void sendMessage(byte[] resp) throws ConnectionException;

    @Override
    void close() throws IOException;
}
