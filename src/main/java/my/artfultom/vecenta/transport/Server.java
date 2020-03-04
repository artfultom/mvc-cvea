package my.artfultom.vecenta.transport;

public interface Server {

    void start(int port);

    boolean register(MethodHandler handler);

    void stop();
}
