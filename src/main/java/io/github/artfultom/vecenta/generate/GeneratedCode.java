package io.github.artfultom.vecenta.generate;

public class GeneratedCode {

    private String pack;
    private String name;
    private String body;

    public GeneratedCode(String pack, String name, String body) {
        this.pack = pack;
        this.name = name;
        this.body = body;
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFullPath() {
        return (pack + "." + name).replace(".", "/") + ".java";
    }

}
