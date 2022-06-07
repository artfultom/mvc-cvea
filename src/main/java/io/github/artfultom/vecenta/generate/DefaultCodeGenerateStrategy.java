package io.github.artfultom.vecenta.generate;

import com.squareup.javapoet.*;
import io.github.artfultom.vecenta.util.GenerateUtils;
import io.github.artfultom.vecenta.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultCodeGenerateStrategy implements CodeGenerateStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultCodeGenerateStrategy.class);

    @Override
    public Map<String, String> generateModels(
            String modelPackage,
            JsonFormatDto dto
    ) {
        Map<String, String> result = new HashMap<>();

        for (JsonFormatDto.Entity entity : dto.getEntities()) {
            for (JsonFormatDto.Entity.Model model : entity.getModels()) {
                String name = StringUtils.capitalizeFirstLetter(model.getName());
                String fullName = modelPackage + "." + name;

                MethodSpec constructor = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .build();

                TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(constructor);

                for (JsonFormatDto.Entity.Param field : model.getFields()) {
                    Class<?> type = convertToTypeName(field.getType());
                    FieldSpec fieldSpec;

                    if (type == null) {
                        String capitalType = StringUtils.capitalizeFirstLetter(field.getType());
                        ClassName className = ClassName.get(modelPackage, capitalType);

                        fieldSpec = FieldSpec.builder(className, field.getName(), Modifier.PUBLIC).build();
                    } else {
                        fieldSpec = FieldSpec.builder(type, field.getName(), Modifier.PUBLIC).build();
                    }

                    builder.addField(fieldSpec);
                    GenerateUtils.addGetterAndSetter(fieldSpec, builder);
                }

                JavaFile file = JavaFile
                        .builder(modelPackage, builder.build())
                        .indent("    ")
                        .skipJavaLangImports(true)
                        .build();

                result.put(fullName, file.toString());
            }
        }

        return result;
    }

    @Override
    public GeneratedCode generateServerCode(
            String filePackage,
            String fileName,
            JsonFormatDto dto
    ) {
        String serverName = fileName.split("\\.")[0];
        String version = fileName.split("\\.")[1];

        String rpcServerBody = generateRpcServerBody(filePackage, version, serverName, dto);

        return new GeneratedCode(serverName, rpcServerBody, version);
    }

    @Override
    public GeneratedCode generateClientCode(
            String filePackage,
            String fileName,
            JsonFormatDto dto
    ) {
        String clientName = dto.getClient();
        String version = fileName.split("\\.")[1];

        String rpcClientBody = generateRpcClientBody(filePackage, version, clientName, dto);

        return new GeneratedCode(clientName, rpcClientBody, version);
    }

    private String generateRpcServerBody(
            String filePackage,
            String version,
            String serverName,
            JsonFormatDto dto
    ) {
        StringBuilder sbRpc = new StringBuilder();
        sbRpc.append("package ").append(filePackage).append(".v").append(version).append(";")
                .append("\n")
                .append("\n");

        sbRpc.append("import io.github.artfultom.vecenta.matcher.Entity;")
                .append("\n")
                .append("\n");

        sbRpc.append("public interface ").append(serverName).append(" {")
                .append("\n")
                .append("\n");

        for (JsonFormatDto.Entity entity : dto.getEntities()) {
            for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                List<String> args = new ArrayList<>();
                for (JsonFormatDto.Entity.Param param : method.getIn()) {
                    String type = translate(param.getType());

                    if (type != null) {
                        args.add(type + " " + param.getName());
                    } else {
                        log.error("Wrong type " + param.getType() + ". Parameter " + param.getName() +
                                " of " + method.getName() + " is ignored.");
                    }
                }

                if (method.getOut().size() == 0) {
                    log.error("No return type in method " + method.getName() + ".");
                    continue;
                }

                String returnType = method.getOut().get(0).getType();
                String translatedReturnType = translate(returnType);
                if (translatedReturnType == null) {
                    log.error("Wrong return type " + returnType + ". Method " + method.getName() + " is ignored.");
                    continue;
                }

                sbRpc
                        .append("    ")
                        .append("@Entity(\"").append(entity.getName()).append("\")")
                        .append("\n");
                sbRpc
                        .append("    ")
                        .append(translatedReturnType).append(" ")
                        .append(method.getName())
                        .append("(")
                        .append(String.join(", ", args))
                        .append(");")
                        .append("\n");
                sbRpc.append("\n");
            }
        }

        sbRpc.append("}\n");

        return sbRpc.toString();
    }

    private String generateRpcClientBody(
            String filePackage,
            String version,
            String clientName,
            JsonFormatDto dto
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(filePackage).append(".v").append(version).append(";")
                .append("\n")
                .append("\n");
        sb.append("import io.github.artfultom.vecenta.exceptions.ProtocolException;")
                .append("\n");
        sb.append("import io.github.artfultom.vecenta.matcher.ConvertParamStrategy;")
                .append("\n");
        sb.append("import io.github.artfultom.vecenta.matcher.impl.DefaultConvertParamStrategy;")
                .append("\n");
        sb.append("import io.github.artfultom.vecenta.transport.Client;")
                .append("\n");
        sb.append("import io.github.artfultom.vecenta.transport.message.Request;")
                .append("\n");
        sb.append("import io.github.artfultom.vecenta.transport.message.Response;")
                .append("\n")
                .append("\n");

        sb.append("import java.net.ConnectException;")
                .append("\n");
        sb.append("import java.util.List;")
                .append("\n")
                .append("\n");

        sb.append("public class ").append(clientName).append(" {")
                .append("\n")
                .append("\n");
        sb
                .append("    ")
                .append("private final Client client;")
                .append("\n")
                .append("\n");
        sb
                .append("    ")
                .append("private final ConvertParamStrategy convertParamStrategy = new DefaultConvertParamStrategy();")
                .append("\n")
                .append("\n");
        sb
                .append("    ")
                .append("public ").append(clientName).append("(Client client) {")
                .append("\n");
        sb
                .append("    ")
                .append("    ")
                .append("this.client = client;")
                .append("\n");
        sb
                .append("    ")
                .append("}")
                .append("\n")
                .append("\n");

        for (JsonFormatDto.Entity entity : dto.getEntities()) {
            for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                List<String> args = new ArrayList<>();
                for (JsonFormatDto.Entity.Param param : method.getIn()) {
                    String type = translate(param.getType());

                    if (type != null) {
                        args.add(type + " " + param.getName());
                    } else {
                        log.error("Wrong type " + param.getType() + ". Parameter " + param.getName() +
                                " of " + method.getName() + " is ignored.");
                    }
                }

                if (method.getOut().size() == 0) {
                    log.error("No return type in method " + method.getName() + ".");
                    continue;
                }

                String returnType = method.getOut().get(0).getType();
                String translatedReturnType = translate(returnType);
                if (translatedReturnType == null) {
                    log.error("Wrong return type " + returnType + ". Method " + method.getName() + " is ignored.");
                    continue;
                }

                sb
                        .append("    ")
                        .append("public ").append(translatedReturnType).append(" ")
                        .append(method.getName())
                        .append("(")
                        .append(String.join(", ", args))
                        .append(") throws ConnectException, ProtocolException {")
                        .append("\n");

                sb
                        .append("    ")
                        .append("    ")
                        .append("Request req = new Request(")
                        .append("\n");

                String argumentTypes = method.getIn()
                        .stream()
                        .map(item -> {
                            String translated = translate(item.getType());
                            if (translated == null) {
                                log.error("Wrong type " + item.getType() + ". Parameter " + item.getName() +
                                        " of " + method.getName() + " is ignored.");
                            }

                            return translated;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(","));
                sb
                        .append("    ")
                        .append("    ")
                        .append("    ")
                        .append("    ")
                        .append("\"").append(entity.getName()).append(".").append(method.getName()).append("(").append(argumentTypes).append(")\",")
                        .append("\n");

                String params = method.getIn().stream()
                        .map(item -> "convertParamStrategy.convertToByteArray(" + translate(item.getType()) + ".class, " + item.getName() + ")")
                        .collect(Collectors.joining(","));

                sb
                        .append("    ")
                        .append("    ")
                        .append("    ")
                        .append("    ")
                        .append("List.of(").append(params).append(")")
                        .append("\n");

                sb
                        .append("    ")
                        .append("    ")
                        .append(");")
                        .append("\n");
                sb.append("\n");
                sb
                        .append("    ")
                        .append("    ")
                        .append("Response resp = client.send(req);")
                        .append("\n");
                sb
                        .append("    ")
                        .append("    ")
                        .append("List<byte[]> result = resp.getResults();")
                        .append("\n");
                sb
                        .append("    ")
                        .append("    ")
                        .append("if (result == null) {")
                        .append("\n");
                sb
                        .append("    ")
                        .append("    ")
                        .append("    ")
                        .append("throw new ProtocolException(resp.getError());")
                        .append("\n");
                sb
                        .append("    ")
                        .append("    ")
                        .append("}")
                        .append("\n");
                sb.append("\n");
                sb
                        .append("    ")
                        .append("    ")
                        .append("return convertParamStrategy.convertToObject(" + translate(method.getOut().get(0).getType()) + ".class, result.get(0));")
                        .append("\n");

                sb.append("    ")
                        .append("}")
                        .append("\n");
            }
        }

        sb.append("}");

        return sb.toString();
    }

    private String translate(String type) {
        switch (type) {
            case "boolean":
                return Boolean.class.getName();
            case "string":
                return String.class.getName();
            case "int8":
            case "uint8":
            case "int16":
                return Short.class.getName();
            case "uint16":
            case "int32":
                return Integer.class.getName();
            case "uint32":
            case "int64":
                return Long.class.getName();
            case "uint64":
                return BigInteger.class.getName();
            default:
                return null;
        }
    }

    private Class<?> convertToTypeName(String type) {
        switch (type) {
            case "boolean":
                return Boolean.class;
            case "string":
                return String.class;
            case "int8":
            case "uint8":
            case "int16":
                return Short.class;
            case "uint16":
            case "int32":
                return Integer.class;
            case "uint32":
            case "int64":
                return Long.class;
            case "uint64":
                return BigInteger.class;
            default:
                return null;
        }
    }
}
