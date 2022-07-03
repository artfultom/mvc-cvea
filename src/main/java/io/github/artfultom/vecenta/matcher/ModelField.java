package io.github.artfultom.vecenta.matcher;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ModelField {

    String type();

}
