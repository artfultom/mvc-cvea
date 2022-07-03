package io.github.artfultom.vecenta.matcher;

import org.junit.Test;

import static org.junit.Assert.*;

public class CollectionTypeTest {

    @Test
    public void get() {
        CollectionType type1 = CollectionType.get("test");
        assertNotNull(type1);
        assertEquals(CollectionType.SIMPLE, type1);
        assertEquals("test", type1.getFirst());
        assertNull(type1.getSecond());

        CollectionType type2 = CollectionType.get("[test]");
        assertNotNull(type2);
        assertEquals(CollectionType.LIST, type2);
        assertEquals("test", type2.getFirst());
        assertNull(type2.getSecond());

        CollectionType type3 = CollectionType.get("[test]testy");
        assertNotNull(type3);
        assertEquals(CollectionType.MAP, type3);
        assertEquals("test", type3.getFirst());
        assertEquals("testy", type3.getSecond());

        CollectionType type4 = CollectionType.get("[test][testy]");
        assertNotNull(type4);
        assertEquals(CollectionType.MAP, type4);
        assertEquals("test", type4.getFirst());
        assertEquals("[testy]", type4.getSecond());
    }
}