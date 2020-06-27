package my.artfultom.vecenta.generate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileGenerator {

    private FileGenerator() {
    }

    public static void generateServerFiles(URL schema) throws URISyntaxException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.json");

        Path path = Paths.get(schema.toURI());

        try (Stream<Path> walk = Files.walk(path, 5)) {
            for (Path p : walk.collect(Collectors.toList())) {
                if (Files.isRegularFile(p) && matcher.matches(p)) {
                    String fileName = p.getFileName().toString();

                    String serverName = fileName.split("\\.")[0];
                    int version = Integer.parseInt(fileName.split("\\.")[1]);

                    JsonFormatDto dto = mapper.readValue(p.toFile(), JsonFormatDto.class);

                    StringBuilder sb = new StringBuilder();
                    sb.append("package my.artfultom.vecenta.controller.v" + version + ";").append("\n").append("\n");
                    sb.append("public interface " + serverName + " {").append("\n");

                    for (JsonFormatDto.Entity entity : dto.getEntities()) {
                        for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                            List<String> args = new ArrayList<>();
                            for (JsonFormatDto.Entity.Method.Param param : method.getIn()) {
                                args.add(param.getType() + " " + param.getName());
                            }

                            sb.append("    ").append(method.getOut().get(0).getType() + " " + method.getName() + "(" + String.join(", ", args) + ");").append("\n");
                        }
                    }

                    sb.append("}\n");

                    Files.writeString(Paths.get(serverName + ".java"), sb.toString());
                }
            }
        }
    }
}
