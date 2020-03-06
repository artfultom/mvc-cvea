package my.artfultom.vecenta.transport;

import my.artfultom.vecenta.matcher.ServerMatcher;

public interface Server {

    void start(int port, ServerMatcher matcher);

    void stop();
}
