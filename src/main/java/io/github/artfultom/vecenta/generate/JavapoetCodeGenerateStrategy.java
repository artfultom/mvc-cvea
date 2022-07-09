package io.github.artfultom.vecenta.generate;

import com.squareup.javapoet.*;
import io.github.artfultom.vecenta.exceptions.ConvertException;
import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.generate.config.GenerateConfiguration;
import io.github.artfultom.vecenta.matcher.*;
import io.github.artfultom.vecenta.matcher.impl.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.Client;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import io.github.artfultom.vecenta.util.StringUtils;

import javax.lang.model.element.Modifier;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavapoetCodeGenerateStrategy implements CodeGenerateStrategy {

    private final GenerateConfiguration configuration;

    public JavapoetCodeGenerateStrategy(GenerateConfiguration configuration) {
        this.configuration = configuration;
    }

    public List<GeneratedCode> generateModels(
            String fileName,
            JsonFormatDto dto
    ) {
        List<GeneratedCode> result = new ArrayList<>();

        String version = fileName.split("\\.")[1];

        for (JsonFormatDto.Client client : dto.getClients()) {
            for (JsonFormatDto.Entity entity : client.getEntities()) {
                for (JsonFormatDto.Entity.Model model : entity.getModels()) {
                    String className = StringUtils.capitalizeFirstLetter(model.getName());
                    String pack = configuration.getModelPackage() + ".v" + version + "." + entity.getName().toLowerCase();

                    MethodSpec constructor = MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .build();

                    String modelName = String.format("%s.%s.%s", client.getName(), entity.getName(), model.getName());
                    AnnotationSpec madelAnnotation = AnnotationSpec.builder(Model.class)
                            .addMember("name", "$S", modelName)
                            .addMember("order", "$L",
                                    model.getFields().stream()
                                            .map(item -> CodeBlock.of("$S", item.getName()))
                                            .collect(CodeBlock.joining(", ", "{", "}"))
                            )
                            .build();

                    TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(constructor)
                            .addAnnotation(madelAnnotation);

                    for (JsonFormatDto.Entity.Param field : model.getFields()) {
                        TypeName typeName = getTypeName(pack, field.getType());

                        AnnotationSpec fieldAnnotation = AnnotationSpec.builder(ModelField.class)
                                .addMember("type", "$S", field.getType())
                                .build();
                        FieldSpec fieldSpec = FieldSpec.builder(typeName, field.getName(), Modifier.PRIVATE)
                                .addAnnotation(fieldAnnotation)
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
            String fileName,
            JsonFormatDto dto
    ) {
        String serverName = fileName.split("\\.")[0];
        String version = fileName.split("\\.")[1];

        String name = StringUtils.capitalizeFirstLetter(serverName);

        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(name)
                .addModifiers(Modifier.PUBLIC);

        String pack = configuration.getServerPackage() + ".v" + version;

        for (JsonFormatDto.Client client : dto.getClients()) {
            for (JsonFormatDto.Entity entity : client.getEntities()) {
                String packWithEntity = pack + "." + entity.getName().toLowerCase();

                for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                    String returnType = StringUtils.fillModelName(
                            List.of(client.getName(), entity.getName()),
                            method.getOut()
                    );
                    AnnotationSpec annotationSpec = AnnotationSpec.builder(RpcMethod.class)
                            .addMember("entity", "\"" + entity.getName() + "\"")
                            .addMember("name", "\"" + method.getName() + "\"")
                            .addMember("argumentTypes", "$L",
                                    method.getIn().stream()
                                            .map(item -> {
                                                String argumentType = StringUtils.fillModelName(
                                                        List.of(client.getName(), entity.getName()),
                                                        item.getType()
                                                );
                                                return CodeBlock.of("$S", argumentType);
                                            })
                                            .collect(CodeBlock.joining(", ", "{", "}"))
                            )
                            .addMember("returnType", "$S", returnType)
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
                String fullPackage = configuration.getClientPackage() + ".v" + version + "." + entity.getName().toLowerCase();

                for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                            .addModifiers(Modifier.PUBLIC)
                            .addException(ConnectException.class)
                            .addException(ProtocolException.class)
                            .addException(ConvertException.class);

                    for (JsonFormatDto.Entity.Param param : method.getIn()) {
                        TypeName typeName = getTypeName(fullPackage, param.getType());
                        ParameterSpec parameterSpec = ParameterSpec.builder(typeName, param.getName()).build();

                        methodBuilder.addParameter(parameterSpec);
                    }

                    String paramNames = method.getIn().stream()
                            .map(JsonFormatDto.Entity.Param::getType)
                            .map(item -> StringUtils.fillModelName(List.of(client.getName(), entity.getName()), item))
                            .collect(Collectors.joining(","));
                    String returnType = StringUtils.fillModelName(
                            List.of(client.getName(), entity.getName()),
                            method.getOut()
                    );
                    String methodName = entity.getName() + "." + method.getName() + "(" + paramNames + ")->" + returnType;
                    methodBuilder.addStatement("String name = $S", methodName);
                    methodBuilder.addStatement("$T<byte[]> arguments = new $T<>()", List.class, ArrayList.class);
                    for (JsonFormatDto.Entity.Param param : method.getIn()) {
                        methodBuilder.addStatement(
                                "arguments.add(convertParamStrategy.convertToByteArray($L))",
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
                    String returnStatement = "return convertParamStrategy.convertToObject(result, $S, $T.class)";

                    TypeName targetType = typeName;
                    if (targetType instanceof ParameterizedTypeName) {
                        targetType = ((ParameterizedTypeName) typeName).rawType;
                    }

                    String modelName = StringUtils.fillModelName(
                            List.of(client.getName(), entity.getName()),
                            method.getOut()
                    );

                    methodBuilder.addStatement(returnStatement, modelName, targetType);

                    builder.addMethod(methodBuilder.build());
                }
            }

            String pack = configuration.getClientPackage() + ".v" + version;

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
        TypeConverter converter = TypeConverter.get(type);
        if (converter == null) {
            return null;
        }

        return converter.getClazz();
    }

    private TypeName getTypeName(String pack, String name) {
        TypeName result;

        CollectionType collectionType = CollectionType.get(name);
        if (collectionType == null) {
            return null;
        }

        Class<?> type = convertToTypeName(collectionType.getFirst());
        if (type == null) {
            String capitalType = StringUtils.capitalizeFirstLetter(collectionType.getFirst());

            result = ClassName.get(pack, capitalType).box();
        } else {
            result = TypeName.get(type);
        }

        if (collectionType == CollectionType.LIST) {
            result = ParameterizedTypeName.get(ClassName.get(List.class), result);
        }

        if (collectionType == CollectionType.MAP) {
            result = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    result,
                    getTypeName(pack, collectionType.getSecond())
            );
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
