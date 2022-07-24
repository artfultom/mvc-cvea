package io.github.artfultom.vecenta.transport.error;

public enum ErrorType {

    WRONG_PROTOCOL("Wrong protocol"),
    WRONG_PROTOCOL_VERSION("Wrong protocol version"),
    WRONG_METHOD_NAME("Wrong method name"),
    UNKNOWN_METHOD_ERROR("Unknown error in a method"),
    CHECKED_ERROR("Checker error");

    private final String message;

    ErrorType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static ErrorType get(int order) {
        for (ErrorType item : ErrorType.values()) {
            if (item.ordinal() == order) {
                return item;
            }
        }

        return null;
    }
}
