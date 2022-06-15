package io.github.artfultom.vecenta.matcher.impl;

import io.github.artfultom.vecenta.matcher.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultConvertParamStrategy implements ConvertParamStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultConvertParamStrategy.class);

    @Override
    public byte[] convertToByteArray(Class<?> clazz, Object in) {
        byte[] result = null;

        Converter converter = Converter.get(clazz);
        if (converter == null) {
            List<Method> methods = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(item -> item.getName().startsWith("get") && item.getParameterTypes().length == 0)
                    .filter(item -> Modifier.isPublic(item.getModifiers()))
                    .sorted(Comparator.comparing(Method::getName))
                    .collect(Collectors.toList());

            try (
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream dataStream = new DataOutputStream(out)
            ) {
                for (Method method : methods) {
                    Object val = method.invoke(in);
                    byte[] bytes = convertToByteArray(method.getReturnType(), val);

                    dataStream.writeInt(bytes.length);
                    dataStream.write(bytes);
                }

                result = out.toByteArray();
            } catch (IOException e) {
                log.error("Cannot open binary stream for type " + clazz.getName(), e);
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("Cannot invoke method. Type " + clazz.getName(), e);
            }
        } else {
            result = converter.convert(in);
        }

        return result;
    }

    @Override
    public <T> T convertToObject(Class<T> clazz, byte[] in) {
        T result = null;

        Converter converter = Converter.get(clazz);
        if (converter == null) {
            List<Method> methods = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(item -> item.getName().startsWith("set") && item.getParameterTypes().length == 1)
                    .filter(item -> Modifier.isPublic(item.getModifiers()))
                    .sorted(Comparator.comparing(Method::getName))
                    .collect(Collectors.toList());

            try {
                T model = clazz.getDeclaredConstructor().newInstance();

                ByteBuffer buf = ByteBuffer.wrap(in);
                for (Method method : methods) {
                    int size = buf.getInt();
                    byte[] dst = new byte[size];
                    buf.get(dst);

                    Class<?> type = method.getParameterTypes()[0];
                    method.invoke(model, convertToObject(type, dst));
                }

                result = model;
            } catch (
                    InvocationTargetException |
                    InstantiationException |
                    IllegalAccessException |
                    NoSuchMethodException e
            ) {
                log.error("Cannot invoke method. Type " + clazz.getName(), e);
            }
        } else {
            result = (T) converter.convert(in);
        }

        return result;
    }
}
