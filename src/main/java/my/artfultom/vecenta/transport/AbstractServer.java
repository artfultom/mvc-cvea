package my.artfultom.vecenta.transport;

public abstract class AbstractServer implements Server {

    private final int PROTOCOL_VERSION = 1;

    protected void handshake(MessageStream stream) {
        byte[] handshake = stream.getNextMessage();
        // TODO handshake process
        stream.sendMessage(new byte[]{0}); // TODO result code
    }
}
