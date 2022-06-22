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
                    for (GeneratedCode model : models) {
                        String other = (model.getPack() + "." + model.getName())
                                .replace(".", "/") + ".java";
                        Path modelFile = config.getDestinationDir().resolve(other);
                        Files.createDirectories(modelFile.getParent());
                        modelFile = Files.writeString(modelFile, model.getBody());
                        result.add(modelFile);
                    }

                    if (config.getMode() != GenerateMode.CLIENT) {
                        GeneratedCode serverCode = strategy.generateServerCode(
                                config.getServerPackage(),
                                fileName,
                                dto
                        );
                        String other = serverCode.getPack()
                                .replace(".", "/") + "/" +
                                serverCode.getName() + ".java";
                        Path serverFile = config.getDestinationDir().resolve(other);
                        Files.createDirectories(serverFile.getParent());
                        serverFile = Files.writeString(serverFile, serverCode.getBody());
                        result.add(serverFile);
                    }

                    if (config.getMode() != GenerateMode.SERVER) {
                        List<GeneratedCode> clients = strategy.generateClientCode(
                                config.getClientPackage(),
                                fileName,
                                dto
                        );

                        for (GeneratedCode client : clients) {
                            String other = client.getPack()
                                    .replace(".", "/") + "/" +
                                    client.getName() + ".java";
                            Path clientFile = config.getDestinationDir().resolve(other);
                            Files.createDirectories(clientFile.getParent());
                            clientFile = Files.writeString(clientFile, client.getBody());

                            result.add(clientFile);
                        }
                    }
                }
            }
        }

        return result;
    }
}
