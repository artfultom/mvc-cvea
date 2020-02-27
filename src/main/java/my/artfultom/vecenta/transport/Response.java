package my.artfultom.vecenta.transport;

import java.util.List;

public class Response {
    private final List<byte[]> params;
    private final Integer errorCode;

    public Response(List<byte[]> params) {
        this.params = params;
        this.errorCode = null;
    }

    public Response(int errorCode) {
        this.params = null;
        this.errorCode = errorCode;
    }

    public List<byte[]> getParams() {
        return params;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
