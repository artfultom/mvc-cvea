package com.github.artfultom.vecenta.transport;

import com.github.artfultom.vecenta.transport.message.Request;
import com.github.artfultom.vecenta.transport.message.Response;

import java.io.IOException;
import java.net.ConnectException;

public interface Client extends AutoCloseable, Cloneable {

    void startConnection(String host, int port) throws ConnectException;

    Response send(Request request) throws ConnectException;

    @Override
    void close() throws IOException;
}
