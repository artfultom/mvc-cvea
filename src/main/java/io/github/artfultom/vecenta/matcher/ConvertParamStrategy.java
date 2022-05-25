package io.github.artfultom.vecenta.matcher;

public interface ConvertParamStrategy {

    byte[] convertToByteArray(Class<?> clazz, Object in);

    Object convertToObject(Class<?> clazz, byte[] in);
}
