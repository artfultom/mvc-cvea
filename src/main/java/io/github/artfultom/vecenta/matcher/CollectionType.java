package io.github.artfultom.vecenta.matcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CollectionType {
    SIMPLE {
        @Override
        public String getFirst(String type) {
            return type;
        }

        @Override
        public String getSecond(String type) {
            return null;
        }
    },
    LIST {
        @Override
        public String getFirst(String type) {
            return type.substring(1, type.length() - 1);
        }

        @Override
        public String getSecond(String type) {
            return null;
        }
    },
    MAP {
        @Override
        public String getFirst(String type) {
            List<String> names = Arrays.stream(type.split("[\\p{Ps}\\p{Pe}]"))
                    .filter(word -> !word.isEmpty())
                    .collect(Collectors.toList());

            String result = type.substring(1).split("]")[0];
            if (!names.get(0).equals(result)) {
                return null;
            }

            return result;
        }

        @Override
        public String getSecond(String type) {
            List<String> names = Arrays.stream(type.split("[\\p{Ps}\\p{Pe}]"))
                    .filter(word -> !word.isEmpty())
                    .collect(Collectors.toList());

            String result = type.substring(type.indexOf("]") + 1);
            if (!result.contains(names.get(1))) {
                return null;
            }

            return result;
        }
    };

    public abstract String getFirst(String type);

    public abstract String getSecond(String type);

    public static CollectionType get(String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }

        List<String> names = Arrays.stream(type.split("[\\p{Ps}\\p{Pe}]"))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toList());

        if (names.size() == 1) {
            if (type.startsWith("[") && type.endsWith("]")) {
                return CollectionType.LIST;
            } else {
                return CollectionType.SIMPLE;
            }
        }

        if (names.size() == 2) {
            return CollectionType.MAP;
        }

        return null;
    }
}
