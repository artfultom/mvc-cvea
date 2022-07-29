package io.github.artfultom.vecenta.transport;

public interface MessageHandler {

    byte[] handle(byte[] bytes);

}
