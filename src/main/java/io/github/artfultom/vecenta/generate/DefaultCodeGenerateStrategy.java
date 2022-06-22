package io.github.artfultom.vecenta.generate;

import com.squareup.javapoet.*;
import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.matcher.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.Converter;
import io.github.artfultom.vecenta.matcher.Model;
import io.github.artfultom.vecenta.matcher.RpcMethod;
import io.github.artfultom.vecenta.matcher.impl.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.Client;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import io.github.artfultom.vecenta.util.StringUtils;

import javax.lang.model.element.Modifier;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultCodeGenerateStrategy implements CodeGenerateStrategy {

    @Override
    public List<GeneratedCode> generateModels(
            String modelPackage,
            String fileName,
            JsonFormatDto dto
    ) {
        List<GeneratedCode> result = new ArrayList<>();

        String version = fileName.split("\\.")[1];

        for (JsonFormatDto.Client client : dto.getClients()) {
            for (JsonFormatDto.Entity entity : client.getEntities()) {
                for (JsonFormatDto.Entity.Model model : entity.getModels()) {
                    String className = StringUtils.capitalizeFirstLetter(model.getName());
                    String pack = modelPackage + ".v" + version + "." + entity.getName().toLowerCase();

                    MethodSpec constructor = MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .build();

                    AnnotationSpec annotationSpec = AnnotationSpec.builder(Model.class)
                            .addMember("order", "$L",
                                    model.getFields().stream()
                                            .map(item -> CodeBlock.of("$S", item.getName()))
                                            .collect(CodeBlock.joining(", ", "{", "}"))
                            )
                            .build();

                    TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(constructor)
                            .addAnnotation(annotationSpec);

                    for (JsonFormatDto.Entity.Param field : model.getFields()) {
                        TypeName typeName = getTypeName(pack, field.getType());

                        FieldSpec fieldSpec = FieldSpec.builder(typeName, field.getName(), Modifier.PUBLIC)
                                .build();

                        builder.addField(fieldSpec);
                        addGetterAndSetter(fieldSpec, builder);
                    }

                    JavaFile file = JavaFile
                            .builder(pack, builder.build())
                            .indent("    ")
                            .skipJavaLangImports(true)
                            .build();

                    result.add(new GeneratedCode(
                            file.packageName,
                            file.typeSpec.name,
                            file.toString()
                    ));
                }
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

        String pack = filePackage + ".v" + version;

        for (JsonFormatDto.Client client : dto.getClients()) {
            for (JsonFormatDto.Entity entity : client.getEntities()) {
                String packWithEntity = pack + "." + entity.getName().toLowerCase();

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
                        TypeName typeName = getTypeName(packWithEntity, param.getType());
                        ParameterSpec parameterSpec = ParameterSpec.builder(typeName, param.getName()).build();

                        methodBuilder.addParameter(parameterSpec);
                    }

                    String returnTypeName = method.getOut();
                    TypeName typeName = getTypeName(packWithEntity, returnTypeName);
                    methodBuilder.returns(typeName);

                    builder.addMethod(methodBuilder.build());
                }
            }
        }

        JavaFile file = JavaFile
                .builder(pack, builder.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build();

        return new GeneratedCode(
                file.packageName,
                file.typeSpec.name,
                file.toString()
        );
    }

    @Override
    public List<GeneratedCode> generateClientCode(
            String filePackage,
            String fileName,
            JsonFormatDto dto
    ) {
        List<GeneratedCode> result = new ArrayList<>();

        for (JsonFormatDto.Client client : dto.getClients()) {
            String clientName = client.getName();
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

            for (JsonFormatDto.Entity entity : client.getEntities()) {
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

            String pack = filePackage + ".v" + version;

            JavaFile file = JavaFile
                    .builder(pack, builder.build())
                    .indent("    ")
                    .skipJavaLangImports(true)
                    .build();

            result.add(new GeneratedCode(
                    file.packageName,
                    file.typeSpec.name,
                    file.toString()
            ));
        }

        return result;
    }

    private Class<?> convertToTypeName(String type) {
        Converter converter = Converter.get(type);
        if (converter == null) {
            return null;
        }

        return converter.getClazz();
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
