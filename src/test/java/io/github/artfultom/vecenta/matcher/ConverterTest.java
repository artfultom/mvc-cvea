package io.github.artfultom.vecenta.matcher;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class ConverterTest {

    @Test
    public void convert() {
        for (Converter converter : Converter.values()) {
            for (int i = 0; i < 100; i++) {
                switch (converter) {
                    case STRING:
                        String stringVal = "" + new Random().nextLong();
                        byte[] stringBytes = converter.convert(stringVal);
                        assertEquals(stringVal.length(), stringBytes.length);

                        String stringVal2 = (String) converter.convert(stringBytes);
                        assertEquals(stringVal, stringVal2);

                        break;
                    case BOOLEAN:
                        Boolean boolVal = new Random().nextBoolean();
                        byte[] boolBytes = converter.convert(boolVal);
                        assertEquals((int) converter.getBytes(), boolBytes.length);

                        Boolean boolVal2 = (Boolean) converter.convert(boolBytes);
                        assertEquals(boolVal, boolVal2);

                        break;
                    default:
                        byte[] bytes = new byte[converter.getBytes()];
                        new Random().nextBytes(bytes);
                        Object val = converter.convert(bytes);
                        byte[] bytes2 = converter.convert(val);
                        assertArrayEquals(bytes, bytes2);
                }
            }
        }
    }

    @Test
    public void getByType() {
        assertEquals(Converter.BOOLEAN, Converter.get("boolean"));
        assertEquals(Converter.INTEGER, Converter.get("int32"));
        assertNull(Converter.get("unknown"));
    }

    @Test
    public void getByClass() {
        assertEquals(Converter.BOOLEAN, Converter.get(Boolean.class));
        assertEquals(Converter.INTEGER, Converter.get(Integer.class));
        assertNull(Converter.get(Thread.class));
    }
}