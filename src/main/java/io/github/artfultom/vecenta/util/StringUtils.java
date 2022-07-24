package io.github.artfultom.vecenta.util;

import io.github.artfultom.vecenta.matcher.TypeConverter;

import java.util.ArrayList;
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
        if (type == null || type.isEmpty()) {
            return Set.of();
        }

        String[] rawSimpleTypes = type
                .replace("[", "'")
                .replace("]", "'")
                .split("'");

        return Arrays.stream(rawSimpleTypes)
                .filter(item -> item != null && !item.isEmpty())
                .collect(Collectors.toSet());
    }

    public static String fillModelName(List<String> path, String type) {
        if (type == null || type.isEmpty()) {
            return type;
        }

        List<String> list = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        for (String s : type.split("")) {
            if (s.equals("[") || s.equals("]")) {
                if (sb.length() > 0) {
                    list.add(sb.toString());
                }
                sb = new StringBuilder();

                list.add(s);
            } else {
                sb.append(s);
            }
        }
        if (sb.length() > 0) {
            list.add(sb.toString());
        }

        list = list.stream()
                .map(item -> {
                    if (item.equals("[") || item.equals("]")) {
                        return item;
                    }

                    if (TypeConverter.get(item) == null) {
                        return String.join(".", path) + "." + item;
                    }

                    return item;
                }).collect(Collectors.toList());

        return String.join("", list);
    }

    public static String getExceptionName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        return Arrays.stream(name.replaceAll("\\W", " ").split(" "))
                .map(item -> {
                    if (item.length() > 1) {
                        return item.substring(0, 1).toUpperCase() + item.substring(1);
                    }

                    return item.toUpperCase();
                }).collect(Collectors.joining()) + "Exception";
    }

    public static String getMethodName(String entity, String method, List<String> arguments, String returnType) {
        if (returnType == null || returnType.isEmpty()) {
            return String.format(
                    "%s.%s(%s)",
                    entity,
                    method,
                    arguments == null ? "" : String.join(",", arguments)
            );
        }

        return String.format("%s.%s(%s)->%s", entity, method, String.join(",", arguments), returnType);
    }
}
