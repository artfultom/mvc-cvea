package io.github.artfultom.vecenta.generation.validation;

import io.github.artfultom.vecenta.exceptions.ValidateException;
import io.github.artfultom.vecenta.generation.JsonFormatDto;

public interface ValidateStrategy {

    void check(String fileName) throws ValidateException;

    void check(JsonFormatDto dto) throws ValidateException;

}
