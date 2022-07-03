package io.github.artfultom.vecenta.matcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CollectionType {
    SIMPLE,
    LIST,
    MAP;

    private String first;

    private String second;

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }

    public static CollectionType get(String type) {
        if (type.startsWith("[")) {
            List<String> names = Arrays.stream(type.substring(1).split("]", 2))
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.toList());

            if (names.size() == 1) {
                CollectionType result = CollectionType.LIST;
                result.first = names.get(0);
                return result;
            }

            if (names.size() == 2) {
                CollectionType result = CollectionType.MAP;
                result.first = names.get(0);
                result.second = names.get(1);
                return result;
            }

            return null;
        }

        CollectionType result = CollectionType.SIMPLE;
        result.first = type;

        return result;
    }
}
