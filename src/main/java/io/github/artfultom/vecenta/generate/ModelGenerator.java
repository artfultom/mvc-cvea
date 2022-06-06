package io.github.artfultom.vecenta.generate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelGenerator {

    private static final String TEMPLATE = "/templates/model.template";

    private final String pack;

    private final String name;

    private final Map<String, String> params;

    public ModelGenerator(String pack, String name, Map<String, String> params) {
        this.pack = pack;
        this.name = name;
        this.params = params;
    }

    public String generate() throws IOException, URISyntaxException {
        URL expectedRes = getClass().getResource(TEMPLATE);

        if (expectedRes != null) {
            Path expected = Path.of(expectedRes.toURI());
            String template = Files.readString(expected);

            List<String> fields = params.entrySet().stream()
                    .map(item -> {
                        String name = item.getKey();
                        String type = item.getValue();

                        return "private " + type + " " + name + ";";
                    })
                    .collect(Collectors.toList());

            List<String> methods = params.entrySet().stream()
                    .map(item -> {
                        String name = item.getKey();
                        String capitalName = name.substring(0, 1).toUpperCase() + name.substring(1);
                        String type = item.getValue();

                        return List.of(
                                "public " + type + " get" + capitalName + " { return " + name + "; }",
                                "public void set" + capitalName + "(" + type + " " + name + ") { this." + name + " = " + name + "; }"
                        );
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            String fieldsStr = fields.stream()
                    .map(item -> "    " + item)
                    .collect(Collectors.joining("\n\n"));

            String methodsStr = methods.stream()
                    .map(item -> "    " + item)
                    .collect(Collectors.joining("\n\n"));

            return template
                    .replace("${package}", pack)
                    .replace("${name}", name)
                    .replace("${fields}", fieldsStr)
                    .replace("${methods}", methodsStr);
        } else {
            // TODO
        }

        return null;
    }
}
