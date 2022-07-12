package io.github.artfultom.vecenta.transport;

public interface MessageStream extends AutoCloseable {

    byte[] getMessage();

    void sendMessage(byte[] resp);
}
