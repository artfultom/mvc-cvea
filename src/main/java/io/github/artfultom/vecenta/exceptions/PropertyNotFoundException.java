package io.github.artfultom.vecenta.exceptions;

public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(String property) {
        super("property " + property + " not found");
    }
}
