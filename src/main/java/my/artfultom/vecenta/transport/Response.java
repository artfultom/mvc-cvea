package my.artfultom.vecenta.transport;

import java.util.List;

public class Response {
    private final List<String> params;
    private final Integer errorCode;

    public Response(List<String> params) {
        this.params = params;
        this.errorCode = null;
    }

    public Response(int errorCode) {
        this.params = null;
        this.errorCode = errorCode;
    }

    public List<String> getParams() {
        return params;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
