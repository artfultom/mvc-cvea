package io.github.artfultom.vecenta.exceptions;

import io.github.artfultom.vecenta.transport.error.MessageError;

public class ProtocolException extends Exception {

    private final MessageError error;

    public ProtocolException(MessageError error) {
        this.error = error;
    }

    public MessageError getError() {
        return error;
    }
}
