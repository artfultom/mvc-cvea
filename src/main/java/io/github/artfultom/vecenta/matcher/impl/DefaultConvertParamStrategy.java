package io.github.artfultom.vecenta.matcher.impl;

import io.github.artfultom.vecenta.matcher.ConvertParamStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
                List<Field> fields = Arrays.stream(clazz.getFields())
//                        .filter(item -> Modifier.isPublic(item.getModifiers()))   // TODO add getters
                        .sorted(Comparator.comparing(Field::getName))   // TODO order of fields
                        .collect(Collectors.toList());

                try (
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        DataOutputStream dataStream = new DataOutputStream(out)
                ) {
                    for (Field field : fields) {
                        Object val = field.get(in);
                        byte[] bytes = convertToByteArray(field.getType(), val);

                        dataStream.writeInt(bytes.length);
                        dataStream.write(bytes);
                    }

                    result = out.toByteArray();
                } catch (IllegalAccessException | IOException e) {
                    throw new RuntimeException(e);  // TODO remove
                }
        }

        return result;
    }

    @Override
    public <T> T convertToObject(Class<T> clazz, byte[] in) {
        Object result;

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
                List<Field> fields = Arrays.stream(clazz.getFields())
//                        .filter(item -> Modifier.isPublic(item.getModifiers()))   // TODO add getters
                        .sorted(Comparator.comparing(Field::getName))   // TODO order of fields
                        .collect(Collectors.toList());

                try {
                    T model = clazz.getDeclaredConstructor().newInstance();

                    ByteBuffer buf = ByteBuffer.wrap(in);
                    for (Field field : fields) {
                        int size = buf.getInt();
                        byte[] dst = new byte[size];
                        buf.get(dst);

                        field.set(model, convertToObject(field.getType(), dst));
                    }

                    result = model;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);  // TODO remove
                }
        }

        return (T) result;
    }
}
