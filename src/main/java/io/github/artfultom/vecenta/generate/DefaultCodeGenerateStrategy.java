package io.github.artfultom.vecenta.generate;

import com.squareup.javapoet.*;
import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.matcher.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.RpcMethod;
import io.github.artfultom.vecenta.matcher.impl.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.Client;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import io.github.artfultom.vecenta.util.StringUtils;

import javax.lang.model.element.Modifier;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultCodeGenerateStrategy implements CodeGenerateStrategy {

    @Override
    public Map<String, String> generateModels(
            String modelPackage,
            String fileName,
            JsonFormatDto dto
    ) {
        Map<String, String> result = new HashMap<>();

        String version = fileName.split("\\.")[1];

        for (JsonFormatDto.Entity entity : dto.getEntities()) {
            for (JsonFormatDto.Entity.Model model : entity.getModels()) {
                String className = StringUtils.capitalizeFirstLetter(model.getName());
                String fullPackage = modelPackage + ".v" + version + "." + entity.getName().toLowerCase();
                String fullName = fullPackage + "." + className;

                MethodSpec constructor = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .build();

                TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(constructor);

                for (JsonFormatDto.Entity.Param field : model.getFields()) {
                    TypeName typeName = getTypeName(fullPackage, field.getType());

                    FieldSpec fieldSpec = FieldSpec.builder(typeName, field.getName(), Modifier.PUBLIC)
                            .build();

                    builder.addField(fieldSpec);
                    addGetterAndSetter(fieldSpec, builder);
                }

                JavaFile file = JavaFile
                        .builder(modelPackage + ".v" + version + "." + entity.getName().toLowerCase(), builder.build())
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

        String name = StringUtils.capitalizeFirstLetter(serverName);

        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(name)
                .addModifiers(Modifier.PUBLIC);

        for (JsonFormatDto.Entity entity : dto.getEntities()) {
            String fullPackage = filePackage + ".v" + version + "." + entity.getName().toLowerCase();

            for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                AnnotationSpec annotationSpec = AnnotationSpec.builder(RpcMethod.class)
                        .addMember("entity", "\"" + entity.getName() + "\"")
                        .addMember("name", "\"" + method.getName() + "\"")
                        .addMember("argumentTypes", "$L",
                                method.getIn().stream()
                                        .map(item -> CodeBlock.of("$S", item.getType()))
                                        .collect(CodeBlock.joining(", ", "{", "}"))
                        )
                        .addMember("returnType", "\"" + method.getOut() + "\"")
                        .build();

                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                        .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                        .addAnnotation(annotationSpec);

                for (JsonFormatDto.Entity.Param param : method.getIn()) {
                    TypeName typeName = getTypeName(fullPackage, param.getType());
                    ParameterSpec parameterSpec = ParameterSpec.builder(typeName, param.getName()).build();

                    methodBuilder.addParameter(parameterSpec);
                }

                String returnTypeName = method.getOut();
                TypeName typeName = getTypeName(fullPackage, returnTypeName);
                methodBuilder.returns(typeName);

                builder.addMethod(methodBuilder.build());
            }
        }

        JavaFile file = JavaFile
                .builder(filePackage + ".v" + version, builder.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build();

        return new GeneratedCode(serverName, file.toString(), version);
    }

    @Override
    public GeneratedCode generateClientCode(
            String filePackage,
            String fileName,
            JsonFormatDto dto
    ) {
        String clientName = dto.getClient();
        String version = fileName.split("\\.")[1];

        String name = StringUtils.capitalizeFirstLetter(clientName);

        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC);

        builder.addField(Client.class, "client", Modifier.PRIVATE, Modifier.FINAL);

        FieldSpec convertParamStrategyField = FieldSpec
                .builder(ConvertParamStrategy.class, "convertParamStrategy")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", DefaultConvertParamStrategy.class)
                .build();
        builder.addField(convertParamStrategyField);

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Client.class, "client")
                .addStatement("this.client = client")
                .build();
        builder.addMethod(constructor);

        for (JsonFormatDto.Entity entity : dto.getEntities()) {
            String fullPackage = filePackage + ".v" + version + "." + entity.getName().toLowerCase();

            for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                        .addModifiers(Modifier.PUBLIC)
                        .addException(ConnectException.class)
                        .addException(ProtocolException.class);

                for (JsonFormatDto.Entity.Param param : method.getIn()) {
                    TypeName typeName = getTypeName(fullPackage, param.getType());
                    ParameterSpec parameterSpec = ParameterSpec.builder(typeName, param.getName()).build();

                    methodBuilder.addParameter(parameterSpec);
                }

                String paramNames = method.getIn().stream()
                        .map(JsonFormatDto.Entity.Param::getType)
                        .collect(Collectors.joining(","));
                String methodName = entity.getName() + "." + method.getName() + "(" + paramNames + ")->" + method.getOut();
                methodBuilder.addStatement("String name = $S", methodName);
                methodBuilder.addStatement("$T<byte[]> arguments = new $T<>()", List.class, ArrayList.class);
                for (JsonFormatDto.Entity.Param param : method.getIn()) {
                    TypeName typeName = getTypeName(fullPackage, param.getType());
                    methodBuilder.addStatement(
                            "arguments.add(convertParamStrategy.convertToByteArray($T.class, $L))",
                            typeName,
                            param.getName()
                    );
                }
                methodBuilder.addStatement("$T req = new $T(name, arguments)", Request.class, Request.class);
                methodBuilder.addCode("\n");

                methodBuilder.addStatement("$T resp = client.send(req)", Response.class);
                methodBuilder.addStatement("byte[] result = resp.getResult()");
                CodeBlock ifNullBlock = CodeBlock.builder()
                        .beginControlFlow("if (result == null)")
                        .addStatement("throw new $T(resp.getError())", ProtocolException.class)
                        .endControlFlow()
                        .build();
                methodBuilder.addCode(ifNullBlock);
                methodBuilder.addCode("\n");

                String returnTypeName = method.getOut();
                TypeName typeName = getTypeName(fullPackage, returnTypeName);
                methodBuilder.returns(typeName);
                String returnStatement = "return convertParamStrategy.convertToObject($T.class, result)";
                methodBuilder.addStatement(returnStatement, typeName);

                builder.addMethod(methodBuilder.build());
            }
        }

        JavaFile file = JavaFile
                .builder(filePackage + ".v" + version, builder.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build();

        return new GeneratedCode(clientName, file.toString(), version);
    }

    private Class<?> convertToTypeName(String type) {
        switch (type) {
            case "boolean":
                return Boolean.class;
            case "string":
                return String.class;
            case "int8":
                return Byte.class;
            case "int16":
                return Short.class;
            case "int32":
                return Integer.class;
            case "int64":
                return Long.class;
            case "dec32":
                return Float.class;
            case "dec64":
                return Double.class;
            // TODO bigint and bigdec
            default:
                return null;
        }
    }

    private TypeName getTypeName(String pack, String name) {
        TypeName result;

        boolean isArray = name.endsWith("[]");
        String simpleName = name.replace("[]", "");

        Class<?> type = convertToTypeName(simpleName);
        if (type == null) {
            String capitalType = StringUtils.capitalizeFirstLetter(simpleName);

            result = ClassName.get(pack, capitalType).box();
        } else {
            result = TypeName.get(type);
        }

        if (isArray) {
            return ParameterizedTypeName.get(ClassName.get(List.class), result);
        }

        return result;
    }

    private void addGetterAndSetter(FieldSpec fieldSpec, TypeSpec.Builder classBuilder) {
        addGetter(fieldSpec, classBuilder);
        addSetter(fieldSpec, classBuilder);
    }

    private void addSetter(FieldSpec fieldSpec, TypeSpec.Builder classBuilder) {
        String setterName = "set" + StringUtils.capitalizeFirstLetter(fieldSpec.name);

        MethodSpec.Builder methodBuilder = MethodSpec
                .methodBuilder(setterName)
                .addModifiers(Modifier.PUBLIC);

        methodBuilder.addParameter(fieldSpec.type, fieldSpec.name);
        methodBuilder.addStatement("this." + fieldSpec.name + " = " + fieldSpec.name);
        classBuilder.addMethod(methodBuilder.build());
    }

    private void addGetter(FieldSpec fieldSpec, TypeSpec.Builder classBuilder) {
        String getterName = "get" + StringUtils.capitalizeFirstLetter(fieldSpec.name);

        MethodSpec.Builder methodBuilder = MethodSpec
                .methodBuilder(getterName)
                .returns(fieldSpec.type)
                .addModifiers(Modifier.PUBLIC);

        methodBuilder.addStatement("return this." + fieldSpec.name);
        classBuilder.addMethod(methodBuilder.build());
    }
}
