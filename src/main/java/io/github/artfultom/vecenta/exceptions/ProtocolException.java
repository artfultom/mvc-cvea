package io.github.artfultom.vecenta.exceptions;

import io.github.artfultom.vecenta.transport.error.ErrorType;

public class ProtocolException extends Exception {

    private final ErrorType error;

    public ProtocolException(ErrorType error) {
        this.error = error;
    }

    public ErrorType getError() {
        return error;
    }
}
