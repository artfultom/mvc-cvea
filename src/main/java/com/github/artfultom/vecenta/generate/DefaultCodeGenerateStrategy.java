package com.github.artfultom.vecenta.generate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultCodeGenerateStrategy implements CodeGenerateStrategy {

    @Override
    public GeneratedCode generateServerCode(
            String filePackage,
            String fileName,
            String body
    ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String serverName = fileName.split("\\.")[0];
        String version = fileName.split("\\.")[1];

        JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(filePackage).append(".v").append(version).append(";")
                .append("\n")
                .append("\n");

        sb.append("import com.github.artfultom.vecenta.matcher.Entity;")
                .append("\n")
                .append("\n");

        sb.append("public interface ").append(serverName).append(" {")
                .append("\n")
                .append("\n");

        for (JsonFormatDto.Entity entity : dto.getEntities()) {
            for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                List<String> args = new ArrayList<>();
                for (JsonFormatDto.Entity.Method.Param param : method.getIn()) {
                    args.add(param.getType() + " " + param.getName());
                }

                sb.append("    ").append("@Entity(\"").append(entity.getName()).append("\")").append("\n");
                sb.append("    ").append(method.getOut().get(0).getType()).append(" ")
                        .append(method.getName())
                        .append("(")
                        .append(String.join(", ", args))
                        .append(");").append("\n");
            }
        }

        sb.append("}\n");

        return new GeneratedCode(serverName, sb.toString(), version);
    }

    @Override
    public GeneratedCode generateClientCode(
            String filePackage,
            String fileName,
            String body
    ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);

        String clientName = dto.getClient();
        String version = fileName.split("\\.")[1];

        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(filePackage).append(".v").append(version).append(";")
                .append("\n")
                .append("\n");
        sb.append("import com.github.artfultom.vecenta.transport.Client;")
                .append("\n");
        sb.append("import com.github.artfultom.vecenta.transport.message.Request;")
                .append("\n");
        sb.append("import com.github.artfultom.vecenta.transport.message.Response;")
                .append("\n")
                .append("\n");

        sb.append("import java.net.ConnectException;")
                .append("\n");
        sb.append("import java.nio.ByteBuffer;")
                .append("\n");
        sb.append("import java.util.List;")
                .append("\n")
                .append("\n");

        sb.append("public class ").append(clientName).append(" {").append("\n");
        sb.append("    ").append("private final Client client;")
                .append("\n")
                .append("\n");
        sb.append("    ").append("public ").append(clientName).append("(Client client) {").append("\n");
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

                sb.append("    ").append("    ").append("Request req = new Request(").append("\n");

                String argumentTypes = method.getIn().stream().map(item -> item.type).collect(Collectors.joining(","));
                sb.append("    ").append("    ").append("    ").append("    ").append("\"").append(entity.getName()).append(".").append(method.getName()).append("(").append(argumentTypes).append(")\",").append("\n");

                // TODO
                sb.append("    ").append("    ").append("    ").append("    ").append("List.of(ByteBuffer.allocate(4).putInt(a).array(), ByteBuffer.allocate(4).putInt(b).array())").append("\n");

                sb.append("    ").append("    ").append(");").append("\n");
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

        return new GeneratedCode(clientName, sb.toString(), version);
    }
}
