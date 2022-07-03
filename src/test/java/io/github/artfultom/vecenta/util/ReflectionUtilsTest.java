package io.github.artfultom.vecenta.util;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;

public class ReflectionUtilsTest {

    @Test
    public void getField() throws NoSuchFieldException {
        class TestClass {
            private int i;

            public int getI() {
                return i;
            }

            public void setI(int i) {
                this.i = i;
            }
        }

        for (Method method : TestClass.class.getDeclaredMethods()) {
            Field field = ReflectionUtils.getField(TestClass.class, method);

            assertNotNull(TestClass.class.getDeclaredField(field.getName()));
            assertTrue(method.getName().toLowerCase().contains(field.getName().toLowerCase()));
        }
    }
}