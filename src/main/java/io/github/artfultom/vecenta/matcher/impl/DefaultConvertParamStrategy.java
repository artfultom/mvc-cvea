package io.github.artfultom.vecenta.matcher.impl;

import io.github.artfultom.vecenta.matcher.ConvertParamStrategy;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DefaultConvertParamStrategy implements ConvertParamStrategy {

    @Override
    public byte[] convertToByteArray(Class<?> clazz, Object in) {
        byte[] result = null;

        switch (clazz.getName()) {
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
                System.err.println("Unknown type: " + clazz.getName());
        }

        return result;
    }

    @Override
    public <T> T convertToObject(Class<T> clazz, byte[] in) {
        Object result = null;

        switch (clazz.getName()) {
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
            case "String":
            case "java.lang.String":
                result = new String(in, StandardCharsets.UTF_8);
                break;
            default:
                System.err.println("Unknown type: " + clazz.getName());
        }

        return (T) result;
    }
}
