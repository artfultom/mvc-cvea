package my.artfultom.vecenta.transport;

import java.util.List;

public class Message {
    private final String methodName;
    private final List<Integer> params; // TODO

    public Message(String methodName, List<Integer> params) {
        this.methodName = methodName;
        this.params = params;
    }
}
