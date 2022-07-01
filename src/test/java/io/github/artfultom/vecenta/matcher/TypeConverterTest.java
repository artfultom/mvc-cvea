package io.github.artfultom.vecenta.matcher;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class TypeConverterTest {

    @Test
    public void convert() {
        for (TypeConverter converter : TypeConverter.values()) {
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
                        assertEquals(Byte.BYTES, boolBytes.length);

                        Boolean boolVal2 = (Boolean) converter.convert(boolBytes);
                        assertEquals(boolVal, boolVal2);

                        break;
                    default:
                        byte[] bytes = new byte[100];
                        new Random().nextBytes(bytes);
                        Object val = converter.convert(bytes);
                        byte[] bytes2 = converter.convert(val);
                        assertArrayEquals(Arrays.copyOf(bytes, bytes2.length), bytes2);
                }
            }
        }
    }

    @Test
    public void getByType() {
        assertEquals(TypeConverter.BOOLEAN, TypeConverter.get("boolean"));
        assertEquals(TypeConverter.INTEGER, TypeConverter.get("int32"));
        assertNull(TypeConverter.get("unknown"));
    }

    @Test
    public void getByClass() {
        assertEquals(TypeConverter.BOOLEAN, TypeConverter.get(Boolean.class));
        assertEquals(TypeConverter.INTEGER, TypeConverter.get(Integer.class));
        assertNull(TypeConverter.get(Thread.class));
    }
}