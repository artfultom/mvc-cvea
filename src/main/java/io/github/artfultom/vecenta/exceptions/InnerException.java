package io.github.artfultom.vecenta.exceptions;

public class InnerException extends RuntimeException {

    public InnerException(String msg, Exception ex) {
        super(msg, ex);
    }
}
