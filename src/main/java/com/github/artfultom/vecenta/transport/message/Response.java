package com.github.artfultom.vecenta.transport.message;

import com.github.artfultom.vecenta.transport.error.MessageError;

import java.util.List;

public class Response {
    private final List<byte[]> results;
    private final MessageError error;

    public Response(List<byte[]> results) {
        this.results = results;
        this.error = null;
    }

    public Response(MessageError error) {
        this.results = null;
        this.error = error;
    }

    public List<byte[]> getResults() {
        return results;
    }

    public MessageError getError() {
        return error;
    }
}
