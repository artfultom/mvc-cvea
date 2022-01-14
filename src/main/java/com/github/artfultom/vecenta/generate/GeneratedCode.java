package com.github.artfultom.vecenta.generate;

public class GeneratedCode {

    private String name;
    private String rpcBody;
    private String httpBody;
    private String version;

    public GeneratedCode(String name, String rpcBody, String httpBody, String version) {
        this.name = name;
        this.rpcBody = rpcBody;
        this.httpBody = httpBody;
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

    public String getHttpBody() {
        return httpBody;
    }

    public void setHttpBody(String httpBody) {
        this.httpBody = httpBody;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
