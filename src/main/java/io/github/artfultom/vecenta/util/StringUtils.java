package io.github.artfultom.vecenta.util;

public class StringUtils {

    private StringUtils() {
    }

    public static String capitalizeFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
