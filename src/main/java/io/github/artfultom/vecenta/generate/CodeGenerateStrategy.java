package io.github.artfultom.vecenta.generate;

import java.util.List;

public interface CodeGenerateStrategy {

    List<GeneratedCode> generateModels(
            String fileName,
            JsonFormatDto dto
    );

    GeneratedCode generateServerCode(
            String fileName,
            JsonFormatDto dto
    );

    List<GeneratedCode> generateClientCode(
            String fileName,
            JsonFormatDto dto
    );
}
