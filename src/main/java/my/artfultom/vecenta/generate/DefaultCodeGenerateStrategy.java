package my.artfultom.vecenta.generate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class DefaultCodeGenerateStrategy implements CodeGenerateStrategy {

    @Override
    public String generateServerCode(String fileName, String body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String serverName = fileName.split("\\.")[0];
        int version = Integer.parseInt(fileName.split("\\.")[1]);

        JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);

        StringBuilder sb = new StringBuilder();
        sb.append("package my.artfultom.vecenta.controller.v").append(version).append(";")
                .append("\n")
                .append("\n");
        sb.append("public interface ").append(serverName).append(" {")
                .append("\n");

        for (JsonFormatDto.Entity entity : dto.getEntities()) {
            for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                List<String> args = new ArrayList<>();
                for (JsonFormatDto.Entity.Method.Param param : method.getIn()) {
                    args.add(param.getType() + " " + param.getName());
                }

                sb.append("    ").append(method.getOut().get(0).getType()).append(" ")
                        .append(method.getName())
                        .append("(")
                        .append(String.join(", ", args))
                        .append(");").append("\n");
            }
        }

        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public String generateClientCode(String fileName, String body) {
        return null;
    }
}
