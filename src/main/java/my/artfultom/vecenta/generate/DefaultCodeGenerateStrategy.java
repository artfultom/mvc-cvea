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
    public String generateClientCode(String fileName, String body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);

        String serverName = fileName.split("\\.")[0];
        int version = Integer.parseInt(fileName.split("\\.")[1]);

        StringBuffer sb = new StringBuffer();

        sb.append("package my.artfultom.vecenta.client.v").append(version).append(";")
                .append("\n")
                .append("\n");
        sb.append("import my.artfultom.vecenta.transport.Client;")
                .append("\n");
        sb.append("import my.artfultom.vecenta.transport.message.Request;")
                .append("\n");
        sb.append("import my.artfultom.vecenta.transport.message.Response;")
                .append("\n")
                .append("\n");

        sb.append("import java.net.ConnectException;")
                .append("\n");
        sb.append("import java.nio.ByteBuffer;")
                .append("\n")
                .append("\n");

        sb.append("public class ClientConnector {").append("\n");
        sb.append("    ").append("private final Client client;")
                .append("\n")
                .append("\n");
        sb.append("    ").append("public ClientConnector(Client client) {").append("\n");
        sb.append("    ").append("    ").append("this.client = client;").append("\n");
        sb.append("    ").append("}")
                .append("\n")
                .append("\n");

        for (JsonFormatDto.Entity entity : dto.getEntities()) {
            for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                List<String> args = new ArrayList<>();
                for (JsonFormatDto.Entity.Method.Param param : method.getIn()) {
                    args.add(param.getType() + " " + param.getName());
                }

                sb.append("    ").append("public ").append(method.getOut().get(0).getType()).append(" ")
                        .append(method.getName())
                        .append("(")
                        .append(String.join(", ", args))
                        .append(") {").append("\n");

                sb.append("    ").append("    ").append("Request req = new Request(null, null);").append("\n");
                sb.append("    ").append("    ").append("try {").append("\n");
                sb.append("    ").append("    ").append("    ").append("Response resp = client.send(req);").append("\n");
                sb.append("    ").append("    ").append("    ").append("return ByteBuffer.wrap(resp.getResults().get(0)).getInt();").append("\n");
                sb.append("    ").append("    ").append("} catch (ConnectException e) {").append("\n");
                sb.append("    ").append("    ").append("    ").append("e.printStackTrace();").append("\n");
                sb.append("    ").append("    ").append("    ").append("return null;").append("\n");
                sb.append("    ").append("    ").append("}").append("\n");


                sb.append("    ").append("}").append("\n");
            }
        }

        sb.append("}");

        return sb.toString();
    }
}
