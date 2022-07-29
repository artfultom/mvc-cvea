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
import io.github.artfultom.vecenta.matcher.annotations.RpcError;
import io.github.artfultom.vecenta.matcher.annotations.RpcMethod;
import io.github.artfultom.vecenta.matcher.param.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.param.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.Connector;
import io.github.artfultom.vecenta.transport.error.ErrorType;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import io.github.artfultom.vecenta.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavapoetClassGenerator implements ClassGenerator {

    private static final Logger log = LoggerFactory.getLogger(JavapoetClassGenerator.class);

    private final GenerateConfiguration configuration;

    private String fileName;

    private Data data;

    private final Map<String, JavaFile> models;

    private final Map<String, JavaFile> exceptions;

    private final List<GeneratedCode> classes;

    public JavapoetClassGenerator(GenerateConfiguration configuration) {
        this.configuration = configuration;

        this.models = new HashMap<>();
        this.exceptions = new HashMap<>();
        this.classes = new ArrayList<>();
    }

    @Override
    public JavapoetBuilder prepare(String fileName, Data data) {
        this.fileName = fileName;
        this.data = data;

        String version = this.fileName.split("\\.")[1];

        for (Data.Client client : this.data.getClients()) {
            for (Data.Entity entity : client.getEntities()) {
                for (Data.Entity.Model model : entity.getModels()) {
                    String className = StringUtils.capitalizeFirstLetter(model.getName());
                    String pack = String.format(
                            "%s.v%s.%s",
                            configuration.getModelPackage(),
                            version,
                            entity.getName().toLowerCase()
                    );

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

                    for (Data.Entity.Param field : model.getFields()) {
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

                    models.put(
                            String.format("%s.%s.%s", client.getName(), entity.getName(), model.getName()),
                            file
                    );
                }

                for (Data.Entity.Method method : entity.getMethods()) {
                    for (String error : method.getErrors()) {
                        String name = StringUtils.getExceptionName(error);
                        String pack = String.format(
                                "%s.v%s.%s",
                                configuration.getExceptionPackage(),
                                version,
                                entity.getName().toLowerCase()
                        );

                        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                                .addModifiers(Modifier.PUBLIC)
                                .superclass(Exception.class);

                        AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(RpcError.class)
                                .addMember("name", "\"" + error + "\"");
                        builder.addAnnotation(annotationSpecBuilder.build());

                        JavaFile file = JavaFile
                                .builder(pack, builder.build())
                                .indent("    ")
                                .skipJavaLangImports(true)
                                .build();

                        exceptions.put(error, file);
                    }
                }
            }
        }

        return new JavapoetBuilder();
    }

    public class JavapoetBuilder implements Builder {

        @Override
        public Builder server() {
            String serverName = fileName.split("\\.")[0];
            String version = fileName.split("\\.")[1];

            String className = StringUtils.capitalizeFirstLetter(serverName);

            TypeSpec.Builder builder = TypeSpec.interfaceBuilder(className)
                    .addModifiers(Modifier.PUBLIC);

            String pack = String.format("%s.v%s", configuration.getServerPackage(), version);

            for (Data.Client client : data.getClients()) {
                for (Data.Entity entity : client.getEntities()) {
                    for (Data.Entity.Method method : entity.getMethods()) {
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

                        for (Data.Entity.Param param : method.getIn()) {
                            TypeName typeName = getModelTypeName(client.getName(), entity.getName(), param.getType());

                            if (typeName == null) {
                                log.warn(String.format("No type for %s.", param.getType()));
                            } else {
                                ParameterSpec parameterSpec = ParameterSpec.builder(typeName, param.getName()).build();
                                methodBuilder.addParameter(parameterSpec);
                            }
                        }

                        if (methodOut != null && !methodOut.isEmpty()) {
                            annotationSpecBuilder.addMember("returnType", "$S", returnType);

                            TypeName typeName = getModelTypeName(client.getName(), entity.getName(), methodOut);
                            methodBuilder.returns(typeName);
                        }

                        if (!method.getErrors().isEmpty()) {
                            annotationSpecBuilder.addMember("errors", "$L",
                                    method.getErrors().stream()
                                            .map(item -> CodeBlock.of("$S", item))
                                            .collect(CodeBlock.joining(", ", "{", "}"))
                            );

                            for (String error : method.getErrors()) {
                                JavaFile ex = exceptions.get(error);
                                if (ex == null) {
                                    log.error(String.format("Cannot find the exception: %s.", error));
                                } else {
                                    methodBuilder.addException(
                                            ClassName.get(ex.packageName, ex.typeSpec.name).box()
                                    );
                                }
                            }
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

            classes.add(new GeneratedCode(file.packageName, file.typeSpec.name, file.toString()));

            return this;
        }

        @Override
        public Builder client() {
            for (Data.Client client : data.getClients()) {
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

                for (Data.Entity entity : client.getEntities()) {
                    String pack = String.format(
                            "%s.v%s.%s",
                            configuration.getClientPackage(),
                            version,
                            entity.getName().toLowerCase()
                    );

                    for (Data.Entity.Method method : entity.getMethods()) {
                        String methodOut = method.getOut();

                        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                                .addModifiers(Modifier.PUBLIC)
                                .addException(ConnectionException.class);

                        if (!method.getIn().isEmpty() || (method.getOut() != null && !method.getOut().isEmpty())) {
                            methodBuilder.addException(ConvertException.class);
                        }

                        for (Data.Entity.Param param : method.getIn()) {
                            TypeName typeName = getModelTypeName(client.getName(), entity.getName(), param.getType());

                            if (typeName != null) {
                                ParameterSpec parameterSpec = ParameterSpec.builder(typeName, param.getName()).build();
                                methodBuilder.addParameter(parameterSpec);
                            }
                        }

                        List<String> paramNames = method.getIn().stream()
                                .map(Data.Entity.Param::getType)
                                .map(item -> StringUtils.fillModelName(List.of(client.getName(), entity.getName()), item))
                                .collect(Collectors.toList());

                        String returnType = StringUtils.fillModelName(
                                List.of(client.getName(), entity.getName()),
                                methodOut
                        );
                        String methodName = StringUtils.getMethodName(
                                entity.getName(),
                                method.getName(),
                                paramNames,
                                returnType
                        );
                        methodBuilder.addStatement("String name = $S", methodName);

                        methodBuilder.addStatement("$T<byte[]> arguments = new $T<>()", List.class, ArrayList.class);
                        for (Data.Entity.Param param : method.getIn()) {
                            methodBuilder.addStatement(
                                    "arguments.add(convertParamStrategy.convertToByteArray($L))",
                                    param.getName()
                            );
                        }
                        methodBuilder.addStatement("$T req = new $T(name, arguments)", Request.class, Request.class);
                        methodBuilder.addCode("\n");

                        methodBuilder.addException(ProtocolException.class);
                        methodBuilder.addStatement("$T resp = connector.send(req)", Response.class);
                        CodeBlock.Builder exceptionBlock = CodeBlock.builder()
                                .beginControlFlow("if (resp.getErrorType() != null)")
                                .beginControlFlow("if (resp.getErrorType() == $T.CHECKED_ERROR)", ErrorType.class);

                        if (method.getErrors().isEmpty()) {
                            exceptionBlock.addStatement("throw new $T()", RuntimeException.class);
                        } else {
                            exceptionBlock.beginControlFlow("switch(resp.getErrorMsg())");
                            for (String error : method.getErrors()) {
                                JavaFile ex = exceptions.get(error);
                                if (ex == null) {
                                    log.error(String.format("Cannot find the exception: %s.", error));
                                } else {
                                    exceptionBlock.addStatement(
                                            "case $S: throw new $T()",
                                            error,
                                            ClassName.get(ex.packageName, ex.typeSpec.name).box()
                                    );
                                }
                            }
                            exceptionBlock.addStatement("default: throw new $T()", RuntimeException.class)
                                    .endControlFlow();
                        }

                        exceptionBlock.endControlFlow()
                                .beginControlFlow("if (resp.getErrorType() == $T.UNKNOWN_METHOD_ERROR)", ErrorType.class)
                                .addStatement("throw new $T()", RuntimeException.class)
                                .endControlFlow()
                                .addStatement("throw new $T(resp.getErrorType())", ProtocolException.class)
                                .endControlFlow();
                        methodBuilder.addCode(exceptionBlock.build());

                        if (methodOut != null && !methodOut.isEmpty()) {
                            methodBuilder.addCode("\n");

                            methodBuilder.addStatement("byte[] result = resp.getResult()");

                            TypeName typeName = getModelTypeName(client.getName(), entity.getName(), methodOut);
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

                        if (!method.getErrors().isEmpty()) {
                            for (String error : method.getErrors()) {
                                JavaFile ex = exceptions.get(error);
                                if (ex == null) {
                                    log.error(String.format("Cannot find the exception: %s.", error));
                                } else {
                                    methodBuilder.addException(ClassName.get(ex.packageName, ex.typeSpec.name).box());
                                }
                            }
                        }

                        builder.addMethod(methodBuilder.build());
                    }

                    JavaFile file = JavaFile
                            .builder(pack, builder.build())
                            .indent("    ")
                            .skipJavaLangImports(true)
                            .build();

                    classes.add(new GeneratedCode(
                            file.packageName,
                            file.typeSpec.name,
                            file.toString()
                    ));
                }
            }

            return this;
        }

        @Override
        public List<GeneratedCode> result() {
            List<GeneratedCode> result = new ArrayList<>();

            result.addAll(models.values().stream()
                    .map(item -> new GeneratedCode(item.packageName, item.typeSpec.name, item.toString()))
                    .collect(Collectors.toList())
            );
            result.addAll(exceptions.values().stream()
                    .map(item -> new GeneratedCode(item.packageName, item.typeSpec.name, item.toString()))
                    .collect(Collectors.toList())
            );
            result.addAll(classes);

            return result;
        }
    }

    private TypeName getTypeName(String pack, String name) {
        TypeName result;

        CollectionType collectionType = CollectionType.get(name);
        if (collectionType == null) {
            return null;
        }

        TypeConverter converter = TypeConverter.get(collectionType.getFirst());
        Class<?> type = converter == null ? null : converter.getClazz();
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

    private TypeName getModelTypeName(String client, String entity, String name) {
        TypeName firstTypeName = null;

        CollectionType collectionType = CollectionType.get(name);
        if (collectionType == null) {
            return null;
        }

        TypeConverter converter = TypeConverter.get(collectionType.getFirst());
        Class<?> firstType = converter == null ? null : converter.getClazz();
        if (firstType == null) {
            JavaFile model = models.get(String.format("%s.%s.%s", client, entity, collectionType.getFirst()));
            if (model == null) {
                return null;
            }

            firstTypeName = ClassName.get(model.packageName, model.typeSpec.name).box();
        } else {
            firstTypeName = TypeName.get(firstType);
        }

        switch (collectionType) {
            case SIMPLE:
                return firstTypeName;
            case LIST:
                return ParameterizedTypeName.get(ClassName.get(List.class), firstTypeName);
            case MAP:
                return ParameterizedTypeName.get(
                        ClassName.get(Map.class),
                        firstTypeName,
                        getModelTypeName(client, entity, collectionType.getSecond())
                );
            default:
                return null;
        }
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
