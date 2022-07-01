package io.github.artfultom.vecenta.matcher;

public interface ConvertParamStrategy {

    byte[] convertToByteArray(Object in);

    <T> T convertToObject(Class<T> clazz, byte[] in);
}
