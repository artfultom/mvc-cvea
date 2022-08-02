package io.github.artfultom.vecenta.matcher;

import org.junit.Test;

import static org.junit.Assert.*;

public class CollectionTypeTest {

    @Test
    public void get() {
        CollectionType type1 = CollectionType.get("test");
        assertNotNull(type1);
        assertEquals(CollectionType.SIMPLE, type1);
        assertEquals("test", type1.getFirst("test"));   // TODO
        assertNull(type1.getSecond("test"));

        CollectionType type21 = CollectionType.get("[test]");
        assertNotNull(type21);
        assertEquals(CollectionType.LIST, type21);
        assertEquals("test", type21.getFirst("[test]"));
        assertNull(type21.getSecond("[test]"));

        CollectionType type22 = CollectionType.get("[[test]]");
        assertNotNull(type22);
        assertEquals(CollectionType.LIST, type22);
        assertEquals("[test]", type22.getFirst("[[test]]"));
        assertNull(type22.getSecond("[[test]]"));

        CollectionType type3 = CollectionType.get("[test]testy");
        assertNotNull(type3);
        assertEquals(CollectionType.MAP, type3);
        assertEquals("test", type3.getFirst("[test]testy"));
        assertEquals("testy", type3.getSecond("[test]testy"));

        CollectionType type41 = CollectionType.get("[test][testy]");
        assertNotNull(type41);
        assertEquals(CollectionType.MAP, type41);
        assertEquals("test", type41.getFirst("[test][testy]"));
        assertEquals("[testy]", type41.getSecond("[test][testy]"));

        CollectionType type42 = CollectionType.get("[test][[testy]]");
        assertNotNull(type42);
        assertEquals(CollectionType.MAP, type42);
        assertEquals("test", type42.getFirst("[test][[testy]]"));
        assertEquals("[[testy]]", type42.getSecond("[test][[testy]]"));
    }
}