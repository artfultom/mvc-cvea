package io.github.artfultom.vecenta.transport.message;

import io.github.artfultom.vecenta.transport.error.MessageError;

public class Response {
    private final byte[] result;
    private final MessageError error;

    public Response(byte[] result) {
        this.result = result;
        this.error = null;
    }

    public Response(MessageError error) {
        this.result = null;
        this.error = error;
    }

    public byte[] getResult() {
        return result;
    }

    public MessageError getError() {
        return error;
    }
}
