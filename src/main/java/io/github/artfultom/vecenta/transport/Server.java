package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.matcher.ServerMatcher;

public interface Server extends AutoCloseable, Cloneable {

    void start(int port, ServerMatcher matcher);

    void close() throws ConnectionException;
}
