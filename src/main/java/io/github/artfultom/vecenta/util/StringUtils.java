package io.github.artfultom.vecenta.util;

import io.github.artfultom.vecenta.matcher.TypeConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StringUtils {

    private StringUtils() {
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase();
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static Set<String> getSimpleTypes(String type) {
        String[] rawSimpleTypes = type
                .replace("[", "'")
                .replace("]", "'")
                .split("'");

        return Arrays.stream(rawSimpleTypes)
                .filter(item -> item != null && !item.isEmpty())
                .collect(Collectors.toSet());
    }

    public static String fillModelName(List<String> path, String type) {
        for (String simple : getSimpleTypes(type)) {
            if (TypeConverter.get(simple) == null) {
                type = type.replaceAll(
                        "\\b" + simple + "\\b",
                        String.join(".", path) + "." + simple
                );
            }
        }

        return type;
    }
}
