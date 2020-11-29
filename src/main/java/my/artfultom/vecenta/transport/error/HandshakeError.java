package my.artfultom.vecenta.transport.error;

public enum HandshakeError {

    WRONG_PROTOCOL("Wrong protocol"),
    WRONG_PROTOCOL_VERSION("Wrong protocol version");

    private String message;

    HandshakeError(String message) {
        this.message = message;
    }
}
