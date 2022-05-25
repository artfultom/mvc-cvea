package io.github.artfultom.vecenta.generate;

public class GeneratedCode {

    private String name;
    private String rpcBody;
    private String version;

    public GeneratedCode(String name, String rpcBody, String version) {
        this.name = name;
        this.rpcBody = rpcBody;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRpcBody() {
        return rpcBody;
    }

    public void setRpcBody(String rpcBody) {
        this.rpcBody = rpcBody;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
