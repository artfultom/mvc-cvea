package io.github.artfultom.vecenta.generate;

import io.github.artfultom.vecenta.exceptions.ValidateException;
import io.github.artfultom.vecenta.matcher.Converter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultValidateStrategy implements ValidateStrategy {

    private static final int MAX_DEPTH_OF_RECURSION = 5;

    @Override
    public void check(String fileName) throws ValidateException {
        if (fileName == null || fileName.isEmpty()) {
            throw new ValidateException("File name is empty.");
        }
        String[] words = fileName.split("\\.");
        if (words.length != 3) {
            throw new ValidateException(String.format("Incorrect file name: %s. It must have tree parts.", fileName));
        }
        if (words[0].isEmpty()) {
            throw new ValidateException(String.format("Incorrect file name: %s. Server name is empty.", fileName));
        }
        if (words[1].isEmpty()) {
            throw new ValidateException(String.format("Incorrect file name: %s. Version is empty.", fileName));
        }
        try {
            Integer.parseInt(words[1]);
        } catch (NumberFormatException e) {
            throw new ValidateException(String.format("Incorrect file name: %s. Version is incorrect.", fileName));
        }
        if (!words[2].equalsIgnoreCase("json")) {
            throw new ValidateException(String.format("Incorrect file name: %s. It must be json.", fileName));
        }
    }

    @Override
    public void check(JsonFormatDto dto) throws ValidateException {
        for (JsonFormatDto.Client client : dto.getClients()) {
            for (JsonFormatDto.Entity entity : client.getEntities()) {
                checkUniqueModel(entity);
                checkMethods(entity);
                checkFields(entity);
                checkRecursion(entity);
            }
        }
    }

    private void checkUniqueModel(JsonFormatDto.Entity entity) throws ValidateException {
        Set<String> modelNames = entity.getModels().stream()
                .map(JsonFormatDto.Entity.Model::getName)
                .filter(item -> Converter.get(item) == null)
                .collect(Collectors.toSet());

        if (modelNames.size() < entity.getModels().size()) {
            throw new ValidateException("Duplicates of models.");
        }
    }

    private void checkMethods(JsonFormatDto.Entity entity) throws ValidateException {
        Set<String> modelNames = entity.getModels().stream()
                .map(JsonFormatDto.Entity.Model::getName)
                .filter(item -> Converter.get(item) == null)
                .collect(Collectors.toSet());

        for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
            String returnType = method.getOut();
            if (Converter.get(returnType) == null && !modelNames.contains(returnType)) {
                throw new ValidateException(String.format(
                        "Incorrect return type %s of method %s.",
                        returnType,
                        method.getName()
                ));
            }

            for (JsonFormatDto.Entity.Param param : method.getIn()) {
                if (Converter.get(param.getType()) == null && !modelNames.contains(param.getType())) {
                    throw new ValidateException(String.format(
                            "Incorrect argument type %s of method %s.",
                            param.getType(),
                            method.getName()
                    ));
                }
            }
        }
    }

    private void checkFields(JsonFormatDto.Entity entity) throws ValidateException {
        Set<String> modelNames = entity.getModels().stream()
                .map(JsonFormatDto.Entity.Model::getName)
                .filter(item -> Converter.get(item) == null)
                .collect(Collectors.toSet());

        for (JsonFormatDto.Entity.Model model : entity.getModels()) {
            for (JsonFormatDto.Entity.Param param : model.getFields()) {
                if (Converter.get(param.getType()) == null && !modelNames.contains(param.getType())) {
                    throw new ValidateException(String.format("Unknown type %s.", param.getType()));
                }
            }
        }
    }

    private void checkRecursion(JsonFormatDto.Entity entity) throws ValidateException {
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
            throw new ValidateException("There is a circle!");
        }
    }
}
