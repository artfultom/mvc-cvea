package io.github.artfultom.vecenta.matcher.impl;

import io.github.artfultom.vecenta.matcher.ConvertParamStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultConvertParamStrategy implements ConvertParamStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultConvertParamStrategy.class);

    @Override
    public byte[] convertToByteArray(Class<?> clazz, Object in) {
        byte[] result = null;

        switch (clazz.getName()) {
            case "boolean":
            case "java.lang.Boolean":
                result = new byte[1];
                result[0] = (byte) ((boolean) in ? 1 : 0);
                break;
            case "char":
            case "java.lang.Character":
                result = ByteBuffer.allocate(Character.BYTES).putChar((Character) in).array();
                break;
            case "short":
            case "java.lang.Short":
                result = ByteBuffer.allocate(Short.BYTES).putShort((Short) in).array();
                break;
            case "int":
            case "java.lang.Integer":
                result = ByteBuffer.allocate(Integer.BYTES).putInt((Integer) in).array();
                break;
            case "long":
            case "java.lang.Long":
                result = ByteBuffer.allocate(Long.BYTES).putLong((Long) in).array();
                break;
            case "float":
            case "java.lang.Float":
                result = ByteBuffer.allocate(Float.BYTES).putFloat((Float) in).array();
                break;
            case "double":
            case "java.lang.Double":
                result = ByteBuffer.allocate(Double.BYTES).putDouble((Double) in).array();
                break;
            case "String":
            case "java.lang.String":
                result = ((String) in).getBytes(StandardCharsets.UTF_8);
                break;
            default:
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
        }

        return result;
    }

    @Override
    public <T> T convertToObject(Class<T> clazz, byte[] in) {
        Object result = null;

        switch (clazz.getName()) {
            case "boolean":
            case "java.lang.Boolean":
                result = ByteBuffer.wrap(in).get() == 1;
                break;
            case "char":
            case "java.lang.Character":
                result = ByteBuffer.wrap(in).getChar();
                break;
            case "short":
            case "java.lang.Short":
                result = ByteBuffer.wrap(in).getShort();
                break;
            case "int":
            case "java.lang.Integer":
                result = ByteBuffer.wrap(in).getInt();
                break;
            case "long":
            case "java.lang.Long":
                result = ByteBuffer.wrap(in).getLong();
                break;
            case "float":
            case "java.lang.Float":
                result = ByteBuffer.wrap(in).getFloat();
                break;
            case "double":
            case "java.lang.Double":
                result = ByteBuffer.wrap(in).getDouble();
                break;
            case "java.lang.String":
                result = new String(in, StandardCharsets.UTF_8);
                break;
            default:
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
        }

        return (T) result;
    }
}
