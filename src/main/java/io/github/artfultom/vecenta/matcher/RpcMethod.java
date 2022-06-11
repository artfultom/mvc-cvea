package io.github.artfultom.vecenta.matcher;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcMethod {
    String entity();

    String name();

    String[] argumentTypes();

    String returnType();
}
