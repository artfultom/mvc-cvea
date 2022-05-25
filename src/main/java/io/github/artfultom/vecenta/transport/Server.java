package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.matcher.ServerMatcher;

import java.io.IOException;

public interface Server extends AutoCloseable, Cloneable {

    void start(int port, ServerMatcher matcher);

    void close() throws IOException;
}
