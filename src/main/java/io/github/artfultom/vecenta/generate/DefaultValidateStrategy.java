package io.github.artfultom.vecenta.generate;

import io.github.artfultom.vecenta.matcher.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultValidateStrategy implements ValidateStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultValidateStrategy.class);

    private static final int MAX_DEPTH_OF_RECURSION = 5;

    @Override
    public boolean isCorrect(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String[] words = fileName.split("\\.");
        if (words.length != 3) {
            log.error("Incorrect file name: " + fileName + ". It must have tree parts.");
            return false;
        }
        if (words[0].isEmpty()) {
            log.error("Incorrect file name: " + fileName + ". Server name is empty.");
            return false;
        }
        if (words[1].isEmpty()) {
            log.error("Incorrect file name: " + fileName + ". Version is empty.");
            return false;
        }
        try {
            Integer.parseInt(words[1]);
        } catch (NumberFormatException e) {
            log.error("Incorrect file name: " + fileName + ". Version is incorrect.");
            return false;
        }
        if (!words[2].equalsIgnoreCase("json")) {
            log.error("Incorrect file name: " + fileName + ". It must be json.");
            return false;
        }

        return true;
    }

    @Override
    public boolean isCorrect(JsonFormatDto dto) {
        for (JsonFormatDto.Client client : dto.getClients()) {
            for (JsonFormatDto.Entity entity : client.getEntities()) {
                Set<String> modelNames = entity.getModels().stream()
                        .map(JsonFormatDto.Entity.Model::getName)
                        .filter(item -> Converter.get(item) == null)
                        .collect(Collectors.toSet());

                if (modelNames.size() < entity.getModels().size()) {
                    log.error("Duplicates of models.");
                    return false;
                }

                for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                    String returnType = method.getOut();
                    if (Converter.get(returnType) == null && !modelNames.contains(returnType)) {
                        log.error("Incorrect return type " + returnType + " of method " + method.getName());
                        return false;
                    }

                    for (JsonFormatDto.Entity.Param param : method.getIn()) {
                        if (Converter.get(param.getType()) == null && !modelNames.contains(param.getType())) {
                            log.error("Incorrect argument type " + param.getType() + " of method " + method.getName());
                            return false;
                        }
                    }
                }

                for (JsonFormatDto.Entity.Model model : entity.getModels()) {
                    for (JsonFormatDto.Entity.Param param : model.getFields()) {
                        if (Converter.get(param.getType()) == null && !modelNames.contains(param.getType())) {
                            log.error("Unknown type " + param.getType());
                            return false;
                        }
                    }
                }

                Map<String, JsonFormatDto.Entity.Model> modelMap = entity.getModels().stream()
                        .collect(Collectors.toMap(
                                JsonFormatDto.Entity.Model::getName,
                                item -> item
                        ));
                List<JsonFormatDto.Entity.Model> models = entity.getModels();
                for (int i = 0; i < MAX_DEPTH_OF_RECURSION; i++) {
                    models = models.stream()
                            .map(JsonFormatDto.Entity.Model::getFields)
                            .flatMap(Collection::stream)
                            .filter(item -> Converter.get(item.getType()) == null)
                            .map(item -> modelMap.get(item.getType()))
                            .collect(Collectors.toList());
                }
                if (!models.isEmpty()) {
                    log.error("There is a circle!");
                    return false;
                }
            }
        }

        return true;
    }
}
