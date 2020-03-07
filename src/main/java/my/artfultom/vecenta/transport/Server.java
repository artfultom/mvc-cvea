package my.artfultom.vecenta.transport;

import my.artfultom.vecenta.matcher.ServerMatcher;

import java.io.IOException;

public interface Server extends AutoCloseable, Cloneable {

    void start(int port, ServerMatcher matcher);

    @Override
    void close() throws IOException;
}
