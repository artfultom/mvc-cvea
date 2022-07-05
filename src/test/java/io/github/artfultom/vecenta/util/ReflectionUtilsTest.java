package io.github.artfultom.vecenta.util;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class ReflectionUtilsTest {

    @Test
    public void getPublicGetters() {
        class TestClass {
            private int i;

            public int getI() {
                return i;
            }

            public void setI(int i) {
                this.i = i;
            }
        }

        List<Method> methods = ReflectionUtils.getPublicGetters(TestClass.class);
        assertNotNull(methods);
        assertEquals(1, methods.size());
        assertEquals("getI", methods.get(0).getName());
    }

    @Test
    public void getPublicSetters() {
        class TestClass {
            private int i;

            public int getI() {
                return i;
            }

            public void setI(int i) {
                this.i = i;
            }
        }

        List<Method> methods = ReflectionUtils.getPublicSetters(TestClass.class);
        assertNotNull(methods);
        assertEquals(1, methods.size());
        assertEquals("setI", methods.get(0).getName());
    }

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

    @Test
    public void findModelClasses() {
        // TODO
    }
}