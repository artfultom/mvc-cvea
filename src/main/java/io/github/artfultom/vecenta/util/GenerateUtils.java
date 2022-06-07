package io.github.artfultom.vecenta.util;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

public class GenerateUtils {

    public static void addGetterAndSetter(FieldSpec fieldSpec, TypeSpec.Builder classBuilder) {
        addGetter(fieldSpec, classBuilder);
        addSetter(fieldSpec, classBuilder);
    }

    public static void addSetter(FieldSpec fieldSpec, TypeSpec.Builder classBuilder) {
        String setterName = "set" + StringUtils.capitalizeFirstLetter(fieldSpec.name);

        MethodSpec.Builder methodBuilder = MethodSpec
                .methodBuilder(setterName)
                .addModifiers(Modifier.PUBLIC);

        methodBuilder.addParameter(fieldSpec.type, fieldSpec.name);
        methodBuilder.addStatement("this." + fieldSpec.name + " = " + fieldSpec.name);
        classBuilder.addMethod(methodBuilder.build());
    }

    public static void addGetter(FieldSpec fieldSpec, TypeSpec.Builder classBuilder) {
        String getterName = "get" + StringUtils.capitalizeFirstLetter(fieldSpec.name);

        MethodSpec.Builder methodBuilder = MethodSpec
                .methodBuilder(getterName)
                .returns(fieldSpec.type)
                .addModifiers(Modifier.PUBLIC);

        methodBuilder.addStatement("return this." + fieldSpec.name);
        classBuilder.addMethod(methodBuilder.build());
    }

}
