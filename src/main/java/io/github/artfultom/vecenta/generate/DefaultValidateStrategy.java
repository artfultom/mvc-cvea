package io.github.artfultom.vecenta.generate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultValidateStrategy implements ValidateStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultValidateStrategy.class);

    @Override
    public boolean isCorrect(String fileName) {
        // TODO logic
        return true;
    }

    @Override
    public boolean isCorrect(JsonFormatDto dto) {
        // TODO logic
        return true;
    }

}
