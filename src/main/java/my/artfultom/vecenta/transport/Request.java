package my.artfultom.vecenta.transport;

import java.util.List;

public class Request {
    private final String methodName;
    private final List<String> params; // TODO

    public Request(String methodName, List<String> params) {
        this.methodName = methodName;
        this.params = params;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getParams() {
        return params;
    }
}
