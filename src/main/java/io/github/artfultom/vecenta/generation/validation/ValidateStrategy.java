package io.github.artfultom.vecenta.generation.validation;

import io.github.artfultom.vecenta.exceptions.ValidateException;
import io.github.artfultom.vecenta.generation.Data;

public interface ValidateStrategy {

    void check(String fileName) throws ValidateException;

    void check(Data data) throws ValidateException;

}
