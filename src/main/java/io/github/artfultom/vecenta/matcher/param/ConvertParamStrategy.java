package io.github.artfultom.vecenta.matcher.param;

import io.github.artfultom.vecenta.exceptions.ConvertException;

public interface ConvertParamStrategy {

    byte[] convertToByteArray(
            Object in
    ) throws ConvertException;

    <T> T convertToObject(
            byte[] in,
            String type,
            Class<T> target
    ) throws ConvertException;
}
