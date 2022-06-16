package io.github.artfultom.vecenta.generate;

import java.util.Map;

public interface CodeGenerateStrategy {

    Map<String, String> generateModels(
            String modelPackage,
            String fileName,
            JsonFormatDto dto
    );

    GeneratedCode generateServerCode(
            String filePackage,
            String fileName,
            JsonFormatDto dto
    );

    GeneratedCode generateClientCode(
            String filePackage,
            String fileName,
            JsonFormatDto dto
    );
}
