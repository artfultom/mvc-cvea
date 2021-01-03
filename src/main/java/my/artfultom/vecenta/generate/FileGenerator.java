package my.artfultom.vecenta.generate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileGenerator {

    private final static int MAX_DEPTH = 5;

    private final CodeGenerateStrategy strategy;

    public FileGenerator(CodeGenerateStrategy strategy) {
        this.strategy = strategy;
    }

    public void generateServerFiles(URL schemaDir) throws URISyntaxException, IOException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.json");

        Path path = Paths.get(schemaDir.toURI());

        try (Stream<Path> walk = Files.walk(path, MAX_DEPTH)) {
            for (Path p : walk.collect(Collectors.toList())) {
                if (Files.isRegularFile(p) && matcher.matches(p)) {
                    String fileName = p.getFileName().toString();
                    String body = Files.readString(p);

                    String result = strategy.generateServerCode(fileName, body);

                    String serverName = fileName.split("\\.")[0];
                    Files.writeString(Paths.get("/Users/artfultom/Documents/IdeaProjects/vecenta/src/main/java/my/artfultom/vecenta/controller/v1/" + serverName + ".java"), result);
                }
            }
        }
    }
}
