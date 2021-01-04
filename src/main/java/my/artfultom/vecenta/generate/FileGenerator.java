package my.artfultom.vecenta.generate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileGenerator {

    private final static int MAX_DEPTH = 5;

    private final CodeGenerateStrategy strategy;

    public FileGenerator(CodeGenerateStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Path> generateFiles(URL schemaDir) throws URISyntaxException, IOException {
        List<Path> result = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.json");

        Path path = Paths.get(schemaDir.toURI());

        try (Stream<Path> walk = Files.walk(path, MAX_DEPTH)) {
            for (Path p : walk.collect(Collectors.toList())) {
                if (Files.isRegularFile(p) && matcher.matches(p)) {
                    String fileName = p.getFileName().toString();
                    String body = Files.readString(p);

                    GeneratedCode serverCode = strategy.generateServerCode(fileName, body);
                    GeneratedCode clientCode = strategy.generateClientCode(fileName, body);

                    Path serverFile = Files.writeString(Paths.get("/Users/artfultom/Documents/IdeaProjects/vecenta/src/main/java/my/artfultom/vecenta/controller/v1/" + serverCode.getName() + ".java"), serverCode.getBody());
                    Path clientFile = Files.writeString(Paths.get("/Users/artfultom/Documents/IdeaProjects/vecenta/src/main/java/my/artfultom/vecenta/client/v1/" + clientCode.getName() + ".java"), clientCode.getBody());

                    result.add(serverFile);
                    result.add(clientFile);
                }
            }
        }

        return result;
    }
}
