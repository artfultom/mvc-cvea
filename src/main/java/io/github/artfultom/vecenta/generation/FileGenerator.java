package io.github.artfultom.vecenta.generation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.artfultom.vecenta.Configuration;
import io.github.artfultom.vecenta.exceptions.ValidateException;
import io.github.artfultom.vecenta.generation.config.GenerateConfiguration;
import io.github.artfultom.vecenta.generation.config.GenerateMode;
import io.github.artfultom.vecenta.generation.validation.DefaultValidateStrategy;
import io.github.artfultom.vecenta.generation.validation.ValidateStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileGenerator {

    private static final Logger log = LoggerFactory.getLogger(FileGenerator.class);

    private static final int MAX_DEPTH = Configuration.getInt("generate.walk_max_depth");

    private CodeGenerateStrategy generateStrategy;
    private ValidateStrategy validateStrategy;

    private final GenerateConfiguration config;

    public FileGenerator(GenerateConfiguration config) {
        this.config = config;
        this.generateStrategy = new JavapoetCodeGenerateStrategy(config);
        this.validateStrategy = new DefaultValidateStrategy();
    }

    public FileGenerator setStrategy(CodeGenerateStrategy generateStrategy) {
        this.generateStrategy = generateStrategy;

        return this;
    }

    public FileGenerator setStrategy(ValidateStrategy validateStrategy) {
        this.validateStrategy = validateStrategy;

        return this;
    }

    public Set<Path> generateFiles() throws IOException {
        Set<Path> result = new HashSet<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.json");
        ObjectMapper mapper = new ObjectMapper();

        Path path = config.getSchemaDir();

        try (Stream<Path> walk = Files.walk(path, MAX_DEPTH)) {
            for (Path p : walk.collect(Collectors.toList())) {
                if (Files.isRegularFile(p) && matcher.matches(p)) {
                    String body = Files.readString(p);
                    JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);

                    String fileName = p.getFileName().toString();
                    try {
                        validateStrategy.check(fileName);
                        validateStrategy.check(dto);
                    } catch (ValidateException e) {
                        log.error(e.getMessage(), e);
                        continue;
                    }

                    List<GeneratedCode> models = generateStrategy.generateModels(
                            fileName,
                            dto
                    );
                    result.addAll(saveModels(config, models));

                    List<GeneratedCode> exceptions = generateStrategy.generateExceptions(
                            fileName,
                            dto
                    );
                    result.addAll(saveExceptions(config, exceptions));

                    if (config.getMode() != GenerateMode.CLIENT) {
                        GeneratedCode server = generateStrategy.generateServerCode(
                                fileName,
                                dto
                        );

                        result.add(saveServer(config, server));
                    }

                    if (config.getMode() != GenerateMode.SERVER) {
                        List<GeneratedCode> clients = generateStrategy.generateClientCode(
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

    private Set<Path> saveModels(GenerateConfiguration config, List<GeneratedCode> models) throws IOException {
        Set<Path> result = new HashSet<>();

        for (GeneratedCode model : models) {
            String other = model.getFullPath();
            Path modelFile = config.getDestinationDir().resolve(other);
            Files.createDirectories(modelFile.getParent());

            Path file = Files.writeString(modelFile, model.getBody());
            result.add(file);
        }

        return result;
    }

    private Set<Path> saveExceptions(GenerateConfiguration config, List<GeneratedCode> exceptions) throws IOException {
        Set<Path> result = new HashSet<>();

        for (GeneratedCode exception : exceptions) {
            String other = exception.getFullPath();
            Path exceptionFile = config.getDestinationDir().resolve(other);
            Files.createDirectories(exceptionFile.getParent());

            Path file = Files.writeString(exceptionFile, exception.getBody());
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

    private Set<Path> saveClients(GenerateConfiguration config, List<GeneratedCode> clients) throws IOException {
        Set<Path> result = new HashSet<>();

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
