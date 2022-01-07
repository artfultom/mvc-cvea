package com.github.artfultom.vecenta.generate;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface CodeGenerateStrategy {

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
