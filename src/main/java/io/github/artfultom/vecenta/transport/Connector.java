package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;

public interface Connector extends AutoCloseable, Cloneable {

    void connect(String host, int port) throws ConnectionException;

    Response send(Request request) throws ConnectionException;

    @Override
    void close() throws ConnectionException;
}
