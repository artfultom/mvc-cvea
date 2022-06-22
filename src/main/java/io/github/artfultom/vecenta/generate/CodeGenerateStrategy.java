package io.github.artfultom.vecenta.generate;

import java.util.List;

public interface CodeGenerateStrategy {

    List<GeneratedCode> generateModels(
            String modelPackage,
            String fileName,
            JsonFormatDto dto
    );

    GeneratedCode generateServerCode(
            String filePackage,
            String fileName,
            JsonFormatDto dto
    );

    List<GeneratedCode> generateClientCode(
            String filePackage,
            String fileName,
            JsonFormatDto dto
    );
}
