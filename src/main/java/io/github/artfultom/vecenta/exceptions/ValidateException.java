package io.github.artfultom.vecenta.exceptions;

public class ValidateException extends Exception {

    private final String message;

    public ValidateException(String message) {
        super();
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
