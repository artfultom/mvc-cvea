package io.github.artfultom.vecenta.generation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileGenerator {

    private static final Logger log = LoggerFactory.getLogger(FileGenerator.class);

    private static final int MAX_DEPTH = Configuration.getInt("generate.walk_max_depth");

    private ClassGenerator classGenerator;
    private ValidateStrategy validateStrategy;

    private final GenerateConfiguration config;

    public FileGenerator(GenerateConfiguration config) {
        this.config = config;
        this.classGenerator = new JavapoetClassGenerator(config);
        this.validateStrategy = new DefaultValidateStrategy();
    }

    public FileGenerator setStrategy(ClassGenerator classGenerator) {
        this.classGenerator = classGenerator;

        return this;
    }

    public FileGenerator setStrategy(ValidateStrategy validateStrategy) {
        this.validateStrategy = validateStrategy;

        return this;
    }

    public Set<Path> generateFiles() throws IOException {
        Set<Path> result = new HashSet<>();

        PathMatcher jsonMatcher = FileSystems.getDefault().getPathMatcher("glob:**.json");
        PathMatcher yamlMatcher = FileSystems.getDefault().getPathMatcher("glob:**.yml");

        ObjectMapper jsonMapper = new ObjectMapper();
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        Path path = config.getSchemaDir();

        try (Stream<Path> walk = Files.walk(path, MAX_DEPTH)) {
            for (Path p : walk.collect(Collectors.toList())) {
                if (Files.isRegularFile(p)) {
                    String body = Files.readString(p);

                    Data data = null;
                    if (jsonMatcher.matches(p)) {
                        data = jsonMapper.readValue(body, Data.class);
                    }
                    if (yamlMatcher.matches(p)) {
                        data = yamlMapper.readValue(body, Data.class);
                    }

                    if (data != null) {
                        String fileName = p.getFileName().toString();
                        try {
                            validateStrategy.check(fileName);
                            validateStrategy.check(data);
                        } catch (ValidateException e) {
                            log.error(e.getMessage(), e);
                            continue;
                        }

                        ClassGenerator.Builder builder = classGenerator.prepare(fileName, data);

                        if (config.getMode() != GenerateMode.CLIENT) {
                            builder.server();
                        }

                        if (config.getMode() != GenerateMode.SERVER) {
                            builder.client();
                        }

                        Set<Path> saved = save(config, builder.result());
                        result.addAll(saved);
                    }
                }
            }
        }

        return result;
    }

    private Set<Path> save(GenerateConfiguration config, List<GeneratedCode> generatedFiles) throws IOException {
        Set<Path> result = new HashSet<>();

        for (GeneratedCode generatedFile : generatedFiles) {
            String other = generatedFile.getFullPath();
            Path modelFile = config.getDestinationDir().resolve(other);
            Files.createDirectories(modelFile.getParent());

            Path file = Files.writeString(modelFile, generatedFile.getBody());
            result.add(file);
        }

        return result;
    }
}
