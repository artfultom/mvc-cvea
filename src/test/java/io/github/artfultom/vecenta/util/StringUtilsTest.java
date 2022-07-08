package io.github.artfultom.vecenta.util;

import org.junit.Test;

import java.util.List;

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

    @Test
    public void getSimpleTypes() {
        assertEquals(List.of("test"), StringUtils.getSimpleTypes("test"));
        assertEquals(List.of("test"), StringUtils.getSimpleTypes("[test]"));
        assertEquals(List.of("test", "test"), StringUtils.getSimpleTypes("[test]test"));
        assertEquals(List.of("test", "test1"), StringUtils.getSimpleTypes("[test]test1"));
        assertEquals(List.of("test", "test1"), StringUtils.getSimpleTypes("[test][test1]"));
    }

    @Test
    public void fillModelName() {
        assertEquals("int32", StringUtils.fillModelName(List.of("client", "entity"),"int32"));
        assertEquals("client.entity.Model", StringUtils.fillModelName(List.of("client", "entity"),"Model"));
        assertEquals("[int32]", StringUtils.fillModelName(List.of("client", "entity"),"[int32]"));
        assertEquals("[client.entity.Model]", StringUtils.fillModelName(List.of("client", "entity"),"[Model]"));
        assertEquals("[int32]int32", StringUtils.fillModelName(List.of("client", "entity"),"[int32]int32"));
        assertEquals("[client.entity.Model]client.entity.Model", StringUtils.fillModelName(List.of("client", "entity"),"[Model]Model"));
        assertEquals("[client.entity.Model][client.entity.Model]", StringUtils.fillModelName(List.of("client", "entity"),"[Model][Model]"));
    }

}