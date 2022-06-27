package io.github.artfultom.vecenta.generate;

import io.github.artfultom.vecenta.exceptions.ValidateException;

public interface ValidateStrategy {

    void check(String fileName) throws ValidateException;

    void check(JsonFormatDto dto) throws ValidateException;

}
