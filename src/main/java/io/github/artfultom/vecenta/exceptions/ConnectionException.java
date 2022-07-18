package io.github.artfultom.vecenta.exceptions;

public class ConnectionException extends Exception {

    private final String message;

    public ConnectionException(String message) {
        this.message = message;
    }

    public ConnectionException(String message, Exception ex) {
        super(ex);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
