package io.github.artfultom.vecenta.exceptions;

public class ConvertException extends Exception {

    private final String message;

    public ConvertException(String message) {
        this.message = message;
    }

    public ConvertException(String message, Exception ex) {
        super(ex);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
