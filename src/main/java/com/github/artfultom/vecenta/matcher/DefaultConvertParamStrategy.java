package com.github.artfultom.vecenta.matcher;

import java.nio.ByteBuffer;

public class DefaultConvertParamStrategy implements ConvertParamStrategy {

    @Override
    public byte[] convertToByteArray(Class<?> clazz, Object in) {
        byte[] result = null;

        switch (clazz.getName()) {
            case "char":
                break;
            case "short":
                break;
            case "int":
            case "java.lang.Integer":
                result = ByteBuffer.allocate(Integer.BYTES).putInt((Integer) in).array();
                break;
            case "long":
                break;
            case "float":
                break;
            case "double":
                break;
            default:
                System.err.println("Unknown type: " + clazz.getName());
        }

        return result;
    }

    @Override
    public Object convertToObject(Class<?> clazz, byte[] in) {
        Object result = null;

        switch (clazz.getName()) {
            case "char":
                result = ByteBuffer.wrap(in).getChar();
                break;
            case "short":
                result = ByteBuffer.wrap(in).getShort();
                break;
            case "int":
            case "java.lang.Integer":
                result = ByteBuffer.wrap(in).getInt();
                break;
            case "long":
                result = ByteBuffer.wrap(in).getLong();
                break;
            case "float":
                result = ByteBuffer.wrap(in).getFloat();
                break;
            case "double":
                result = ByteBuffer.wrap(in).getDouble();
                break;
            default:
                System.err.println("Unknown type: " + clazz.getName());
        }

        return result;
    }
}
