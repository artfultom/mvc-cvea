package io.github.artfultom.vecenta.generate;

public interface ValidateStrategy {

    boolean isCorrect(String fileName);

    boolean isCorrect(JsonFormatDto dto);

}
