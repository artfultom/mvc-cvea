package io.github.artfultom.vecenta.matcher;

import io.github.artfultom.vecenta.util.ReflectionUtils;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractConvertParamStrategy implements ConvertParamStrategy {

    protected Map<String, Class<?>> models;

    protected AbstractConvertParamStrategy() {
        try {
            this.models = ReflectionUtils.findModelClasses().stream()
                    .collect(Collectors.toMap(
                            item -> item.getAnnotation(Model.class).name(),
                            item -> item
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);  // TODO
        }
    }
}
