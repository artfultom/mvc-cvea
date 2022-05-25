package io.github.artfultom.vecenta.generate;

import io.github.artfultom.vecenta.Configuration;
import io.github.artfultom.vecenta.generate.config.GenerateConfiguration;
import io.github.artfultom.vecenta.generate.config.GenerateMode;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileGenerator {

    private final static int maxDepth = Configuration.getInt("generate.walk_max_depth");

    private final CodeGenerateStrategy strategy;

    public FileGenerator(CodeGenerateStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Path> generateFiles(GenerateConfiguration config) throws IOException {
        List<Path> result = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.json");

        Path path = config.getSchemaDir();

        try (Stream<Path> walk = Files.walk(path, maxDepth)) {
            for (Path p : walk.collect(Collectors.toList())) {
                if (Files.isRegularFile(p) && matcher.matches(p)) {
                    String fileName = p.getFileName().toString();
                    String body = Files.readString(p);

                    if (config.getMode() != GenerateMode.CLIENT) {
                        GeneratedCode serverCode = strategy.generateServerCode(
                                config.getServerPackage(),
                                fileName,
                                body
                        );
                        Path serverFile = config.getDestinationDir().resolve(config.getServerPackage().replace(".", "/") + "/v" + serverCode.getVersion() + "/" + serverCode.getName() + ".java");
                        Files.createDirectories(serverFile.getParent());
                        serverFile = Files.writeString(serverFile, serverCode.getRpcBody());
                        result.add(serverFile);
                    }

                    if (config.getMode() != GenerateMode.SERVER) {
                        GeneratedCode clientCode = strategy.generateClientCode(
                                config.getClientPackage(),
                                fileName,
                                body
                        );
                        Path clientFile = config.getDestinationDir().resolve(config.getClientPackage().replace(".", "/") + "/v" + clientCode.getVersion() + "/" + clientCode.getName() + ".java");
                        Files.createDirectories(clientFile.getParent());
                        clientFile = Files.writeString(clientFile, clientCode.getRpcBody());
                        result.add(clientFile);
                    }
                }
            }
        }

        return result;
    }
}
