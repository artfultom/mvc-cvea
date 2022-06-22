package io.github.artfultom.vecenta.generate;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final int MAX_DEPTH = Configuration.getInt("generate.walk_max_depth");

    private final CodeGenerateStrategy strategy;

    public FileGenerator(CodeGenerateStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Path> generateFiles(GenerateConfiguration config) throws IOException {
        List<Path> result = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.json");
        ObjectMapper mapper = new ObjectMapper();

        Path path = config.getSchemaDir();

        try (Stream<Path> walk = Files.walk(path, MAX_DEPTH)) {
            for (Path p : walk.collect(Collectors.toList())) {
                if (Files.isRegularFile(p) && matcher.matches(p)) {
                    String fileName = p.getFileName().toString();
                    String body = Files.readString(p);
                    JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);

                    List<GeneratedCode> models = strategy.generateModels(
                            config.getModelPackage(),
                            fileName,
                            dto
                    );

                    result.addAll(saveModels(config, models));

                    if (config.getMode() != GenerateMode.CLIENT) {
                        GeneratedCode server = strategy.generateServerCode(
                                config.getServerPackage(),
                                fileName,
                                dto
                        );

                        result.add(saveServer(config, server));
                    }

                    if (config.getMode() != GenerateMode.SERVER) {
                        List<GeneratedCode> clients = strategy.generateClientCode(
                                config.getClientPackage(),
                                fileName,
                                dto
                        );

                        result.addAll(saveClients(config, clients));
                    }
                }
            }
        }

        return result;
    }

    private List<Path> saveModels(GenerateConfiguration config, List<GeneratedCode> models) throws IOException {
        List<Path> result = new ArrayList<>();

        for (GeneratedCode model : models) {
            String other = model.getFullPath();
            Path modelFile = config.getDestinationDir().resolve(other);
            Files.createDirectories(modelFile.getParent());

            Path file = Files.writeString(modelFile, model.getBody());
            result.add(file);
        }

        return result;
    }

    private Path saveServer(GenerateConfiguration config, GeneratedCode server) throws IOException {
        String other = server.getFullPath();
        Path serverFile = config.getDestinationDir().resolve(other);
        Files.createDirectories(serverFile.getParent());

        return Files.writeString(serverFile, server.getBody());
    }

    private List<Path> saveClients(GenerateConfiguration config, List<GeneratedCode> clients) throws IOException {
        List<Path> result = new ArrayList<>();

        for (GeneratedCode client : clients) {
            String other = client.getFullPath();
            Path clientFile = config.getDestinationDir().resolve(other);
            Files.createDirectories(clientFile.getParent());

            Path file = Files.writeString(clientFile, client.getBody());
            result.add(file);
        }

        return result;
    }
}
