package io.github.artfultom.vecenta.generate;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public interface CodeGenerateStrategy {

    Map<String, String> generateModels(
            String modelPackage,
            String body
    ) throws JsonProcessingException;

    GeneratedCode generateServerCode(
            String filePackage,
            String fileName,
            String body
    ) throws JsonProcessingException;

    GeneratedCode generateClientCode(
            String filePackage,
            String fileName,
            String body
    ) throws JsonProcessingException;
}
