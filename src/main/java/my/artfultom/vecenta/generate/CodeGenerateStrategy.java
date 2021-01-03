package my.artfultom.vecenta.generate;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface CodeGenerateStrategy {

    String generateServerCode(String fileName, String body) throws JsonProcessingException;

    String generateClientCode(String fileName, String body);
}
