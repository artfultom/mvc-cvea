package my.artfultom.vecenta.transport.message;

import java.util.List;

public class Response {
    private final List<byte[]> results;
    private final int errorCode;

    public Response(List<byte[]> results) {
        this.results = results;
        this.errorCode = 0;
    }

    public Response(int errorCode) {
        this.results = null;
        this.errorCode = errorCode;
    }

    public List<byte[]> getResults() {
        return results;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
