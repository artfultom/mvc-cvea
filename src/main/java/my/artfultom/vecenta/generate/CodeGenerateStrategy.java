package my.artfultom.vecenta.generate;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface CodeGenerateStrategy {

    GeneratedCode generateServerCode(String fileName, String body) throws JsonProcessingException;

    GeneratedCode generateClientCode(String fileName, String body) throws JsonProcessingException;
}
