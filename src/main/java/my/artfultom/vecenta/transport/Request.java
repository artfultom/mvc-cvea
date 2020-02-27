package my.artfultom.vecenta.transport;

import java.util.List;

public class Request {
    private final String methodName;
    private final List<byte[]> params;

    public Request(String methodName, List<byte[]> params) {
        this.methodName = methodName;
        this.params = params;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<byte[]> getParams() {
        return params;
    }
}
