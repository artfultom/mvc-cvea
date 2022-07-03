package io.github.artfultom.vecenta.matcher;

public interface ConvertParamStrategy {

    byte[] convertToByteArray(Object in);

    <T> T convertToObject(byte[] in, String type, Class<T> target);
}
