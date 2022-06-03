package io.github.artfultom.vecenta.generate.config;

import java.nio.file.Path;

public class GenerateConfiguration {

    private Path schemaDir;

    private Path destinationDir;

    private String serverPackage;

    private String clientPackage;

    private GenerateMode mode;

    public GenerateConfiguration(
            Path schemaDir,
            Path destinationDir,
            String serverPackage,
            String clientPackage
    ) {
        this.schemaDir = schemaDir;
        this.destinationDir = destinationDir;
        this.serverPackage = serverPackage;
        this.clientPackage = clientPackage;
        this.mode = GenerateMode.ALL;
    }

    public GenerateConfiguration(
            Path schemaDir,
            Path destinationDir,
            String serverPackage,
            String clientPackage,
            GenerateMode mode
    ) {
        this.schemaDir = schemaDir;
        this.destinationDir = destinationDir;
        this.serverPackage = serverPackage;
        this.clientPackage = clientPackage;
        this.mode = mode;
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

    public GenerateMode getMode() {
        return mode;
    }

    public void setMode(GenerateMode mode) {
        this.mode = mode;
    }
}
