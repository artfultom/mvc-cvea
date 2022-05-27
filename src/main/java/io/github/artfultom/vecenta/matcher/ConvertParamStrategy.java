package io.github.artfultom.vecenta.matcher;

public interface ConvertParamStrategy {

    byte[] convertToByteArray(Class<?> clazz, Object in);

    <T> T convertToObject(Class<T> clazz, byte[] in);
}
