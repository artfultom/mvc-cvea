package my.artfultom.vecenta.generate;

import java.io.IOException;
import java.net.URI;
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

    public List<Path> generateFiles(URI schemaDir, URI dirsDir) throws IOException {
        List<Path> result = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.json");

        Path path = Paths.get(schemaDir);

        try (Stream<Path> walk = Files.walk(path, MAX_DEPTH)) {
            for (Path p : walk.collect(Collectors.toList())) {
                if (Files.isRegularFile(p) && matcher.matches(p)) {
                    String fileName = p.getFileName().toString();
                    String body = Files.readString(p);

                    GeneratedCode serverCode = strategy.generateServerCode(fileName, body);
                    GeneratedCode clientCode = strategy.generateClientCode(fileName, body);

                    Path serverFile = Path.of(dirsDir).resolve("my/artfultom/vecenta/controller/v1/" + serverCode.getName() + ".java");
                    Files.createDirectories(serverFile.getParent());
                    Path clientFile = Path.of(dirsDir).resolve("my/artfultom/vecenta/client/v1/" + clientCode.getName() + ".java");
                    Files.createDirectories(clientFile.getParent());

                    serverFile = Files.writeString(serverFile, serverCode.getBody());
                    clientFile = Files.writeString(clientFile, clientCode.getBody());

                    result.add(serverFile);
                    result.add(clientFile);
                }
            }
        }

        return result;
    }
}
