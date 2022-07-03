package io.github.artfultom.vecenta.util;

import java.util.Arrays;
import java.util.List;
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

    public static List<String> getSimpleTypes(String type) {
        String[] rawSimpleTypes = type
                .replace("[", "'")
                .replace("]", "'")
                .split("'");

        return Arrays.stream(rawSimpleTypes)
                .filter(item -> item != null && !item.isEmpty())
                .collect(Collectors.toList());
    }
}
