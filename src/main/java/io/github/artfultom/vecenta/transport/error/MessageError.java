package io.github.artfultom.vecenta.transport.error;

public enum MessageError {

    WRONG_PROTOCOL("Wrong protocol"),
    WRONG_PROTOCOL_VERSION("Wrong protocol version"),
    WRONG_METHOD_NAME("Wrong method name");

    private final String message;

    MessageError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static MessageError get(int order) {
        for (MessageError item : MessageError.values()) {
            if (item.ordinal() == order) {
                return item;
            }
        }

        return null;
    }
}
