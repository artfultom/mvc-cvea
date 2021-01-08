package my.artfultom.vecenta.generate;

import java.nio.file.Path;

public class Configuration {

    private Path schemaDir;
    private Path destinationDir;
    private String serverPackage;
    private String clientPackage;

    public Configuration(Path schemaDir, Path destinationDir, String serverPackage, String clientPackage) {
        this.schemaDir = schemaDir;
        this.destinationDir = destinationDir;
        this.serverPackage = serverPackage;
        this.clientPackage = clientPackage;
    }

    public Path getSchemaDir() {
        return schemaDir;
    }

    public void setSchemaDir(Path schemaDir) {
        this.schemaDir = schemaDir;
    }

    public Path getDestinationDir() {
        return destinationDir;
    }

    public void setDestinationDir(Path destinationDir) {
        this.destinationDir = destinationDir;
    }

    public String getServerPackage() {
        return serverPackage;
    }

    public void setServerPackage(String serverPackage) {
        this.serverPackage = serverPackage;
    }

    public String getClientPackage() {
        return clientPackage;
    }

    public void setClientPackage(String clientPackage) {
        this.clientPackage = clientPackage;
    }
}
