package my.artfultom.vecenta.transport;

public interface MessageStream extends AutoCloseable {

    byte[] getNextMessage();

    void sendMessage(byte[] resp);
}
