package io.github.artfultom.vecenta.transport;

public interface MessageStream extends AutoCloseable {

    byte[] getNextMessage();

    void sendMessage(byte[] resp);
}
