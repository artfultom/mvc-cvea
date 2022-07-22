package io.github.artfultom.vecenta.generation;

import com.squareup.javapoet.*;
import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.exceptions.ConvertException;
import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.generation.config.GenerateConfiguration;
import io.github.artfultom.vecenta.matcher.CollectionType;
import io.github.artfultom.vecenta.matcher.TypeConverter;
import io.github.artfultom.vecenta.matcher.annotations.Model;
import io.github.artfultom.vecenta.matcher.annotations.ModelField;
import io.github.artfultom.vecenta.matcher.annotations.RpcMethod;
import io.github.artfultom.vecenta.matcher.param.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.param.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.Connector;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import io.github.artfultom.vecenta.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavapoetCodeGenerateStrategy implements CodeGenerateStrategy {

    private static final Logger log = LoggerFactory.getLogger(JavapoetCodeGenerateStrategy.class);

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

                    TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                            .addModifiers(Modifier.PUBLIC);

                    MethodSpec constructor = MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .build();
                    builder.addMethod(constructor);

                    String modelName = String.format("%s.%s.%s", client.getName(), entity.getName(), model.getName());
                    AnnotationSpec madelAnnotation = AnnotationSpec.builder(Model.class)
                            .addMember("name", "$S", modelName)
                            .addMember("order", "$L",
                                    model.getFields().stream()
                                            .map(item -> CodeBlock.of("$S", item.getName()))
                                            .collect(CodeBlock.joining(", ", "{", "}"))
                            )
                            .build();
                    builder.addAnnotation(madelAnnotation);

                    for (JsonFormatDto.Entity.Param field : model.getFields()) {
                        TypeName typeName = getTypeName(pack, field.getType());

                        if (typeName == null) {
                            log.warn(String.format("No type for %s.", field.getType()));
                        } else {
                            AnnotationSpec fieldAnnotation = AnnotationSpec.builder(ModelField.class)
                                    .addMember("type", "$S", field.getType())
                                    .build();
                            FieldSpec fieldSpec = FieldSpec.builder(typeName, field.getName(), Modifier.PRIVATE)
                                    .addAnnotation(fieldAnnotation)
                                    .build();

                            builder.addField(fieldSpec);
                            addGetterAndSetter(fieldSpec, builder);
                        }
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
    public List<GeneratedCode> generateExceptions(String fileName, JsonFormatDto dto) {
        List<GeneratedCode> result = new ArrayList<>();

        String version = fileName.split("\\.")[1];

        for (JsonFormatDto.Client client : dto.getClients()) {
            for (JsonFormatDto.Entity entity : client.getEntities()) {


                for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                    for (String error : method.getErrors()) {
                        String name = StringUtils.getExceptionName(error);
                        String pack = configuration.getModelPackage() + ".v" + version + "." + entity.getName().toLowerCase();  // TODO format

                        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                                .addModifiers(Modifier.PUBLIC)
                                .superclass(Exception.class);

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

        String className = StringUtils.capitalizeFirstLetter(serverName);

        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(className)
                .addModifiers(Modifier.PUBLIC);

        String pack = String.format("%s.v%s", configuration.getServerPackage(), version);

        for (JsonFormatDto.Client client : dto.getClients()) {
            for (JsonFormatDto.Entity entity : client.getEntities()) {
                String packWithEntity = pack + "." + entity.getName().toLowerCase();

                for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                    String methodOut = method.getOut();
                    String returnType = StringUtils.fillModelName(
                            List.of(client.getName(), entity.getName()),
                            methodOut
                    );
                    AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(RpcMethod.class)
                            .addMember("entity", "\"" + entity.getName() + "\"")
                            .addMember("name", "\"" + method.getName() + "\"");

                    if (!method.getIn().isEmpty()) {
                        annotationSpecBuilder.addMember("argumentTypes", "$L",
                                method.getIn().stream()
                                        .map(item -> {
                                            String argumentType = StringUtils.fillModelName(
                                                    List.of(client.getName(), entity.getName()),
                                                    item.getType()
                                            );
                                            return CodeBlock.of("$S", argumentType);
                                        })
                                        .collect(CodeBlock.joining(", ", "{", "}"))
                        );
                    }

                    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC);

                    for (JsonFormatDto.Entity.Param param : method.getIn()) {
                        TypeName typeName = getTypeName(packWithEntity, param.getType());

                        if (typeName == null) {
                            log.warn(String.format("No type for %s.", param.getType()));
                        } else {
                            ParameterSpec parameterSpec = ParameterSpec.builder(typeName, param.getName()).build();
                            methodBuilder.addParameter(parameterSpec);
                        }
                    }

                    if (methodOut != null && !methodOut.isEmpty()) {
                        annotationSpecBuilder.addMember("returnType", "$S", returnType);

                        TypeName typeName = getTypeName(packWithEntity, methodOut);
                        methodBuilder.returns(typeName);
                    }

                    methodBuilder.addAnnotation(annotationSpecBuilder.build());

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

            builder.addField(Connector.class, "connector", Modifier.PRIVATE, Modifier.FINAL);

            FieldSpec convertParamStrategyField = FieldSpec
                    .builder(ConvertParamStrategy.class, "convertParamStrategy")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("new $T()", DefaultConvertParamStrategy.class)
                    .build();
            builder.addField(convertParamStrategyField);

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Connector.class, "connector")
                    .addStatement("this.connector = connector")
                    .build();
            builder.addMethod(constructor);

            for (JsonFormatDto.Entity entity : client.getEntities()) {
                String fullPackage = String.format(
                        "%s.v%s.%s",
                        configuration.getClientPackage(),
                        version,
                        entity.getName().toLowerCase()
                );

                for (JsonFormatDto.Entity.Method method : entity.getMethods()) {
                    String methodOut = method.getOut();

                    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                            .addModifiers(Modifier.PUBLIC)
                            .addException(ConnectionException.class)
                            .addException(ConvertException.class);

                    for (JsonFormatDto.Entity.Param param : method.getIn()) {
                        TypeName typeName = getTypeName(fullPackage, param.getType());

                        if (typeName != null) {
                            ParameterSpec parameterSpec = ParameterSpec.builder(typeName, param.getName()).build();
                            methodBuilder.addParameter(parameterSpec);
                        }
                    }

                    String paramNames = method.getIn().stream()
                            .map(JsonFormatDto.Entity.Param::getType)
                            .map(item -> StringUtils.fillModelName(List.of(client.getName(), entity.getName()), item))
                            .collect(Collectors.joining(","));

                    if (methodOut == null || methodOut.isEmpty()) {
                        String methodName = String.format("%s.%s(%s)", entity.getName(), method.getName(), paramNames);
                        methodBuilder.addStatement("String name = $S", methodName);
                    } else {
                        String returnType = StringUtils.fillModelName(
                                List.of(client.getName(), entity.getName()),
                                methodOut
                        );

                        String methodName = String.format(
                                "%s.%s(%s)->%s",
                                entity.getName(),
                                method.getName(),
                                paramNames,
                                returnType
                        );
                        methodBuilder.addStatement("String name = $S", methodName);
                    }

                    methodBuilder.addStatement("$T<byte[]> arguments = new $T<>()", List.class, ArrayList.class);
                    for (JsonFormatDto.Entity.Param param : method.getIn()) {
                        methodBuilder.addStatement(
                                "arguments.add(convertParamStrategy.convertToByteArray($L))",
                                param.getName()
                        );
                    }
                    methodBuilder.addStatement("$T req = new $T(name, arguments)", Request.class, Request.class);
                    methodBuilder.addCode("\n");

                    if (methodOut == null || methodOut.isEmpty()) {
                        methodBuilder.addStatement("connector.send(req)");
                    } else {
                        methodBuilder.addException(ProtocolException.class);

                        methodBuilder.addStatement("$T resp = connector.send(req)", Response.class);
                        methodBuilder.addStatement("byte[] result = resp.getResult()");
                        CodeBlock ifNullBlock = CodeBlock.builder()
                                .beginControlFlow("if (result == null)")
                                .addStatement("throw new $T(resp.getError())", ProtocolException.class)
                                .endControlFlow()
                                .build();
                        methodBuilder.addCode(ifNullBlock);

                        methodBuilder.addCode("\n");

                        TypeName typeName = getTypeName(fullPackage, methodOut);
                        methodBuilder.returns(typeName);
                        String returnStatement = "return convertParamStrategy.convertToObject(result, $S, $T.class)";

                        TypeName targetType = typeName;
                        if (targetType instanceof ParameterizedTypeName) {
                            targetType = ((ParameterizedTypeName) typeName).rawType;
                        }

                        String modelName = StringUtils.fillModelName(
                                List.of(client.getName(), entity.getName()),
                                methodOut
                        );

                        methodBuilder.addStatement(returnStatement, modelName, targetType);
                    }

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
