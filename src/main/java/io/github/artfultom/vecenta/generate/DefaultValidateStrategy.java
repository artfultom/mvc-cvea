package io.github.artfultom.vecenta.generate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultValidateStrategy implements ValidateStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultValidateStrategy.class);

    @Override
    public boolean isCorrect(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String[] words = fileName.split("\\.");
        if (words.length != 3) {
            return false;
        }
        if (words[0].isEmpty()) {
            return false;
        }
        if (words[1].isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(words[1]);
        } catch (NumberFormatException nfe) {
            return false;
        }
        if (!words[2].equalsIgnoreCase("json")) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isCorrect(JsonFormatDto dto) {
        // TODO logic
        return true;
    }

}
