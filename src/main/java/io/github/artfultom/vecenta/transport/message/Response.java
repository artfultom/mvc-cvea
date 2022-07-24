package io.github.artfultom.vecenta.transport.message;

import io.github.artfultom.vecenta.transport.error.ErrorType;

public class Response {
    private final byte[] result;

    private final ErrorType errorType;

    private final String errorMsg;

    public Response(byte[] result) {
        this.result = result;
        this.errorType = null;
        this.errorMsg = null;
    }

    public Response(ErrorType errorType) {
        this.result = null;
        this.errorType = errorType;
        this.errorMsg = null;
    }

    public Response(ErrorType errorType, String errorMsg) {
        this.result = null;
        this.errorType = errorType;
        this.errorMsg = errorMsg;
    }

    public byte[] getResult() {
        return result;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
