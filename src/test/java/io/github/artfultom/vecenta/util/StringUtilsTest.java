package io.github.artfultom.vecenta.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

    @Test
    public void capitalizeFirstLetter() {
        assertEquals("Test", StringUtils.capitalizeFirstLetter("test"));
        assertEquals("TEST", StringUtils.capitalizeFirstLetter("TEST"));
        assertEquals("TEST", StringUtils.capitalizeFirstLetter("tEST"));
        assertEquals("", StringUtils.capitalizeFirstLetter(""));
        assertEquals("A", StringUtils.capitalizeFirstLetter("a"));
    }
}